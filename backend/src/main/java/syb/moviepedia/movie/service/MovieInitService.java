package syb.moviepedia.movie.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.movie.external.tmdb.TmdbClient;
import syb.moviepedia.movie.external.tmdb.dto.TmdbInitMovie;
import syb.moviepedia.movie.external.tmdb.dto.TmdbInitMovieList;
import syb.moviepedia.movie.repository.MovieRepository;

/**
 * 영화 초기 데이터 설정을 위한 클래스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MovieInitService {

    private final TmdbClient tmdbClient;
    private final MovieRepository movieRepository;

    public TmdbInitMovieList init() {
        return tmdbClient.getInitMovies();

        // 데이터 가공

        // 리포지토리 저장
    }
}
