package syb.moviepedia.movie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import syb.moviepedia.movie.domain.MovieDocument;
import syb.moviepedia.movie.dto.response.KeywordResponse;
import syb.moviepedia.movie.repository.MovieSearchRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MovieSearchService {

    private final MovieSearchRepository mvSearchRepository;

    public void save(KeywordResponse request) {
        MovieDocument movieDoc = MovieDocument.builder()
                .id(request.code())
                .title(request.title())
                .build();

        mvSearchRepository.save(movieDoc);
    }

    public List<KeywordResponse> getKeywords(String keyword) {
        log.info("요청 키워드 : {}", keyword);
        List<MovieDocument> results = mvSearchRepository.findByTitle(keyword);
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

}
