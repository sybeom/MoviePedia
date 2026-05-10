package syb.moviepedia.movie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.common.MovieCategoryType;
import syb.moviepedia.movie.domain.Country;
import syb.moviepedia.movie.domain.Genre;
import syb.moviepedia.movie.domain.Movie;
import syb.moviepedia.movie.domain.MovieCategory;
import syb.moviepedia.movie.external.tmdb.TmdbClient;
import syb.moviepedia.movie.external.tmdb.dto.TmdbInitMovie;
import syb.moviepedia.movie.external.tmdb.dto.TmdbInitMovieList;
import syb.moviepedia.movie.external.tmdb.dto.TmdbMovieList;
import syb.moviepedia.movie.repository.GenreRepository;
import syb.moviepedia.movie.repository.MovieCategoryRepository;
import syb.moviepedia.movie.repository.MovieRepository;
import syb.moviepedia.movie.repository.TmdbCountryRepository;

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
    private final MovieCategoryRepository movieCategoryRepository;
    private final GenreRepository tmdbGenreRepository;
    private final TmdbCountryRepository tmdbCountryRepository;

    public void initMovies() {
        if (movieRepository.count() > 0) {
            return;
        }
        log.info("initMovies() 일반 영화 초기 데이터 호출");
        saveMovies(tmdbClient.getInitMovies());
    }

    public void initCategoryMovies() {
        log.info("initCategoryMovies() 카테고리 영화 초기 데이터 호출");

        refreshCategoryMovies(MovieCategoryType.POPULAR, tmdbClient.getPopularMovies());
        refreshCategoryMovies(MovieCategoryType.UPCOMING, tmdbClient.getUpcomingMovies());
        refreshCategoryMovies(MovieCategoryType.NOW_PLAYING, tmdbClient.getNowPlayingMovies());
    }

    private void saveMovies(TmdbMovieList responses) {
        List<Movie> movies = responses.results().stream()
                .filter(response -> !movieRepository.existsByMovieId(response.movieId())) // DB에 없는 영화들만
                .map(response -> toMovie(response))
                .toList();

        movieRepository.saveAll(movies);
    }

    // 카테고리 영화 목록 저장 또는 업데이트
    private void refreshCategoryMovies(MovieCategoryType category, TmdbMovieList responses) {
        // 기존 카테고리 영화 모두 삭제
        movieCategoryRepository.deleteByCategoryType(category);

        // 카테고리별 영화 새로 갱신
        for (TmdbInitMovie response: responses.results()) {
            Movie movie = saveOrUpdateMovie(response); // 영화DB에 영화가 존재하면 갱신, 없다면 저장

            MovieCategory mc = MovieCategory.builder()
                    .categoryType(category)
                    .movie(movie)
                    .popularity(response.popularity())
                    .build();

            movieCategoryRepository.save(mc);
        }
    }

    // 장르 데이터 초기화(로드)
    public void initGenres() {
        // tmdb 장르 데이터는 총 19개
        if (tmdbGenreRepository.count() > 0) {
            return;
        }

        log.info("initGenres() 실행, 장르 데이터 변경 감지");
        List<Genre> genres = tmdbClient.getMovieGenres().genres().stream()
                .filter(genre ->
                    !tmdbGenreRepository.existsByGenreId(genre.id())) // 존재하지 않는 값들만
                .map(genre ->
                        Genre.builder()
                                .genreId(genre.id())
                                .name(genre.name())
                                .build())
                .toList();
        tmdbGenreRepository.saveAll(genres);
    }

    // 국가 데이터 초기화
    public void initCountries() {
        if (tmdbCountryRepository.count() > 0) {
            return;
        }
        log.info("initCountries() 실행, 국가 데이터 변경 감지");
        List<Country> countries = tmdbClient.getCountries().stream()
                .map(country ->
                        Country.builder()
                                .code(country.code())
                                .name(country.name())
                                .build())
                .toList();
        tmdbCountryRepository.saveAll(countries);
    }

    // TmdbInitMovie -> Movie 엔티티로 가공
    private Movie toMovie(TmdbInitMovie response) {
        return Movie.builder()
                .movieId(response.movieId())
                .title(response.title())
                .backdropPath(response.backdropPath())
                .posterPath(response.posterPath())
                .genres(getGenreNames(response.genres()))
                .overview(response.overview())
                .releaseDate(response.releaseDate())
                .country(response.country())
                .runtime(response.runtime())
                .globalRating(response.globalRating())
                .build();
    }

    // Tmdb 영화 -> Movie 엔티티 가공
    private List<Movie> toMovies(TmdbInitMovieList tmdbMovieList) {
        // TODO: 백드롭 및 포스터 완전 경로로 변경, 관람 등급 설정, 국가 설정, 글로벌 평점 소숫점 변경, runtime 설정
        // TODO: 국가 및 관람 등급도 별도로 가져와야한다.
        return tmdbMovieList.results().stream()
                .map(movie ->
                        Movie.builder()
                                .movieId(movie.movieId())
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

    // 영화 저장 또는 갱신
    private Movie saveOrUpdateMovie(TmdbInitMovie response) {
        return movieRepository.findByMovieId(response.movieId()) // 영화 id에 해당하는 영화 찾기
                .map(movie -> {
                    log.info("saveOrUpdateMovie() 영화 정보 갱신 됨");
                    movie.updateFrom(response); // 존재하면 영화 정보 업데이트(값들이 변경되어있을 수 있기때문)
                    return movie;
                })
                .orElseGet(() -> movieRepository.save(toMovie(response))); // 존재하지 않으면 db 저장
    }

    // 장르 id에 대응하는 장르명 가져오기
    private List<String> getGenreNames(List<Integer> genres) {
        return genres.stream().map(genreId ->
                tmdbGenreRepository.findNameByGenreId(genreId)
        ).toList();
    }
}
