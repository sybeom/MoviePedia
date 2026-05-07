package syb.moviepedia.movie.external.tmdb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import syb.moviepedia.common.exception.TmdbApiException;
import syb.moviepedia.movie.dto.MovieDetailDto;
import syb.moviepedia.movie.external.tmdb.dto.*;

import java.util.List;

/**
 * TMDB API 호출하는 클라이언트 클래스
 * 인기, 개봉예정, 현재 상영 API는 장르가 이름이 아닌 번호로 오기때문에 장르 번호와 장르명을 응답 받는 장르 API를 별도 호출한다
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbClient {

    private final WebClient tmdbWebClient;

    // 전체 영화
    public TmdbInitMovieList getInitMovies() {
        return fetchInitMovies("/discover/movie");
    }

    // 인기 영화
    public TmdbMovieSummaryList getPopularMovies() {
        return fetchMovieSummaryList("/movie/popular");
    }

    // 현재 상영 영화
    public TmdbMovieSummaryList getNowPlayingMovies() {
        return fetchMovieSummaryList("/movie/now_playing");
    }

    // 개봉 예정작 api 호출
    public TmdbMovieSummaryList getUpcomingMovies() {
        return fetchMovieSummaryList("/movie/upcoming");
    }

    // 영화 장르 정보
    public TmdbGenreList getMovieGenres() {
        return fetchMovieGenres(); // api 호출
    }

    // 관람 등급
    public TmdbMovieCertification getMovieCertification(Long movieId) {
        return fetchMovieReleaseDate(movieId); // 개봉일 api로 관람등급을 얻는다.
    }

    // 영화 상세
    public MovieDetailDto getMovieDetail(Long movieId) {
        return fetchMovieDetail(movieId);
    }

    // 초기 영화 호출
    private TmdbInitMovieList fetchInitMovies(String path) {
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                                    .path(path)
                                    .queryParam("language", "ko-KR")
                                    .queryParam("page",1)
                                    .queryParam("region", "KR")
                                    .build())
                    .retrieve()
                    .bodyToMono(TmdbInitMovieList.class)
                    .block();
        } catch (Exception e) {
            throw new TmdbApiException("TMDB 영화 초기화 API 호출 실패. path=" + path, e);
        }
    }

    // 영화 api 호출  // TODO: 주석 고치기 카테고리 API로
    private TmdbMovieSummaryList fetchMovieSummaryList(String path) {
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("language", "ko-KR")
                            .queryParam("page", 1)
                            .queryParam("region", "KR") // 한국 지역 기준으로 개봉일, 공개 여부, 결과 순서 등을 맞춘다.
                            .build())
                    .retrieve()
                    .bodyToMono(TmdbMovieSummaryList .class) // 응답 json에서 해당 필드에 맞게 데이터가 자동 매핑된다.
                    .block();
        } catch (Exception e) {
            throw new TmdbApiException("TMDB 영화 목록 API 호출 실패. path=" + path, e);
        }
    }

    // 장르 api 호출
    private TmdbGenreList fetchMovieGenres() {
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/genre/movie/list")
                            .queryParam("language", "ko-KR")
                            .build())
                    .retrieve()
                    .bodyToMono(TmdbGenreList.class)
                    .block();
        } catch (Exception e) {
            throw new TmdbApiException("TMDB 장르 목록 API 호출 실패.", e);
        }
    }

    // 개봉일 api 호출 - 연령 등급 얻기(개봉일 api에서 영화 연령 등급을 얻을 수 있기때문)
    private TmdbMovieCertification fetchMovieReleaseDate(Long movieId) {
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/movie/" + movieId + "/release_dates")
                            .queryParam("language", "ko-KR")
                            .queryParam("region", "KR")
                            .build())
                    .retrieve()
                    .bodyToMono(TmdbMovieCertification.class)
                    .block();
        } catch (Exception e) {
            throw new TmdbApiException("TMDB 개봉 날짜 API 호출 실패", e);
        }
    }

    // 영화 상세 api 호출 (영화 상세는 변환할 데이터가 크게 없기 때문에 Dto 클래스로 받고 그대로 반환)
    private MovieDetailDto fetchMovieDetail(Long movieId) {
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/movie/{movieId}")
                            .queryParam("language", "ko-KR")
                            .queryParam("region", "KR")
                            .build(movieId))
                    .retrieve()
                    .bodyToMono(MovieDetailDto.class)
                    .block();
        } catch (Exception e) {
            log.error("TMDB 개봉 날짜 API 호출 실패. movieId={}", movieId, e);
            throw new TmdbApiException("TMDB 영화 상세 API 호출 실패", e);
        }
    }
}
