package syb.moviepedia.movie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.movie.domain.TmdbGenre;
import syb.moviepedia.movie.external.tmdb.TmdbClient;
import syb.moviepedia.movie.external.tmdb.dto.TmdbGenreList;
import syb.moviepedia.movie.external.tmdb.dto.TmdbGenreResponse;
import syb.moviepedia.movie.external.tmdb.dto.TmdbInitMovie;
import syb.moviepedia.movie.external.tmdb.dto.TmdbInitMovieList;
import syb.moviepedia.movie.repository.MovieRepository;
import syb.moviepedia.movie.repository.TmdbGenreRepository;

import java.util.List;

/**
 * 영화 초기 데이터 설정을 위한 클래스
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MovieInitService {

    private final TmdbClient tmdbClient;
    private final MovieRepository movieRepository;
    private final TmdbGenreRepository tmdbGenreRepository;

    public TmdbInitMovieList init() {
        return tmdbClient.getInitMovies();

        // 데이터 가공

        // 리포지토리 저장
    }

    // 장르 데이터 초기화(로드)
    public void initGenres() {
        // tmdb 장르 데이터는 총 19개
        if (tmdbGenreRepository.count() > 0) {
            return;
        }

        log.info("initGenres() 실행, 장르 데이터 변경 감지");
        List<TmdbGenre> genres = tmdbClient.getMovieGenres().genres().stream()
                .filter(genre ->
                    !tmdbGenreRepository.existsByGenreId(genre.id()))
                .map(genre ->
                        TmdbGenre.builder()
                                .genreId(genre.id())
                                .name(genre.name())
                                .build())
                .toList();
        tmdbGenreRepository.saveAll(genres);
    }
}
