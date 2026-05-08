package syb.moviepedia.movie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.movie.domain.Movie;
import syb.moviepedia.movie.domain.TmdbGenre;
import syb.moviepedia.movie.external.tmdb.TmdbClient;
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
        // 데이터 가공
        List<Movie> movies = toMovies(tmdbClient.getInitMovies());

        // 리포지토리 저장
        movieRepository.saveAll(movies);

        return tmdbClient.getInitMovies();
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

    // Tmdb 영화 -> Movie 엔티티 가공
    private List<Movie> toMovies(TmdbInitMovieList tmdbMovieList) {
        // TODO: 백드롭 및 포스터 완전 경로로 변경, 관람 등급 설정, 국가 설정, 글로벌 평점 소숫점 변경, runtime 설정
        return tmdbMovieList.results().stream()
                .map(movie ->
                        Movie.builder()
                                .movieId(movie.id())
                                .title(movie.title())
                                .backdropPath(movie.backdropPath())
                                .posterPath(movie.posterPath())
                                .genres(getGenreNames(movie.genres()))
                                .overview(movie.overview())
                                .releaseDate(movie.releaseDate())
                                .country(movie.country())
                                .runtime(movie.runtime())
                                .globalRating(movie.globalRating())
                                .build())
                .toList();
    }

    // 장르 id에 대응하는 장르명 가져오기
    private List<String> getGenreNames(List<Integer> genres) {
        return genres.stream().map(genreId ->
                tmdbGenreRepository.findNameByGenreId(genreId)
        ).toList();
    }
}
