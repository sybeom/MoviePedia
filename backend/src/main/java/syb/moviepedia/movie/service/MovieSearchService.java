package syb.moviepedia.movie.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.movie.domain.Movie;
import syb.moviepedia.movie.domain.MovieDocument;
import syb.moviepedia.movie.dto.response.AllMoviesResponse;
import syb.moviepedia.movie.dto.response.KeywordResponse;
import syb.moviepedia.movie.repository.MovieRepository;
import syb.moviepedia.movie.repository.MovieSearchRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MovieSearchService {

    private final MovieSearchRepository mvSearchRepo;
    private final MovieRepository mvRepo;

    public void save(KeywordResponse request) {
        MovieDocument movieDoc = MovieDocument.builder()
                .id(request.code())
                .title(request.title())
                .build();

        mvSearchRepo.save(movieDoc);
    }

    public List<KeywordResponse> getKeywords(String keyword) {
        log.info("요청 키워드 : {}", keyword);
        List<MovieDocument> results = mvSearchRepo.findByTitle(keyword);
        log.info("키워드 목록 호출 결과 : {}", results);
        return toKeywordsResponse(results);
    }

    private List<KeywordResponse> toKeywordsResponse(List<MovieDocument> results) {
        return results.stream().map(doc ->
                        KeywordResponse.builder()
                                .code(doc.getId())
                                .title(doc.getDisplayTitle())
                                .build())
                .toList();
    }

    public List<AllMoviesResponse> getKeywordMovies(String keyword) {
        List<MovieDocument> mvDocs = mvSearchRepo.findByTitle(keyword);

        if (mvDocs.isEmpty()) {
            return List.of();
        }

        // 코드 추출
        List<Integer> movieCodes = mvDocs.stream()
                .map(doc -> Integer.parseInt(doc.getId()))
                .distinct()
                .toList();

        List<Movie> movies = mvRepo.findMoviesByCodeIn(movieCodes); // 코드 기반 영화들 조회

        return movies.stream().map(mv -> AllMoviesResponse.from(mv)).toList();
    }
}
