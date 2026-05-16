package syb.moviepedia.movie.external.tmdb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import syb.moviepedia.common.exception.TmdbApiException;
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
    //TODO : 경로 상수화 하기
    private final WebClient tmdbWebClient;

    // 전체 영화
    public TmdbMovieList getInitMovies() {
        return fetchInitMovies("/discover/movie");
    }

    // 인기 영화
    public TmdbMovieList getPopularMovies() {
        return fetchMovieList("/movie/popular");
    }

    // 현재 상영 영화
    public TmdbMovieList getNowPlayingMovies() {
        return fetchMovieList("/movie/now_playing");
    }

    // 개봉 예정
    public TmdbMovieList getUpcomingMovies() {
        return fetchMovieList("/movie/upcoming");
    }

    // 장르 정보
    public TmdbGenreList getMovieGenres() {
        return fetchMovieGenres(); // api 호출
    }

    // 관람 등급
    public TmdbMovieCertification getMovieCertification(Long movieId) {
        // 개봉일 api로 관람등급을 얻는다.
        return fetchMovieReleaseDate(movieId);
    }

    // 국가 정보
    public List<TmdbCountry> getCountries() {
        return fetchCountries();
    }

    // 영화 상세
    public TmdbMovieDetail getMovieDetail(Long movieId) {
        return fetchMovieDetail(movieId);
    }

    // 크레딧 (감독, 출연) 정보
    public TmdbCredit getCredit(Long movieId) {
        return fetchCredit(movieId);
    }

    // 초기 영화 호출
    private TmdbMovieList fetchInitMovies(String path) {
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                                    .path(path)
                                    .queryParam("language", "ko-KR")
                                    .queryParam("page",1)
                                    .queryParam("region", "KR")
                                    .build())
                    .retrieve()
                    .bodyToMono(TmdbMovieList.class)
                    .block();
        } catch (Exception e) {
            throw new TmdbApiException("TMDB 영화 초기화 API 호출 실패. path=" + path, e);
        }
    }

    // 카테고리 영화 목록 api 호출
    private TmdbMovieList fetchMovieList(String path) {
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("language", "ko-KR")
                            .queryParam("page", 1)
                            .queryParam("region", "KR") // 한국 지역 기준으로 개봉일, 공개 여부, 결과 순서 등을 맞춘다.
                            .build())
                    .retrieve()
                    .bodyToMono(TmdbMovieList .class) // 응답 json에서 해당 필드에 맞게 데이터가 자동 매핑된다.
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

    // 국가 코드 정보 API 호출
    private List<TmdbCountry> fetchCountries() {
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/configuration/countries")
                            .queryParam("language", "ko-KR")
                            .build())
                    .retrieve()
                    .bodyToFlux(TmdbCountry.class) // 응답의 최상위 구조가 객체가 아니라 배열이기때문에 List로 곧바로 받아야한다.
                    .collectList()
                    .block();
        } catch (Exception e) {
            throw new TmdbApiException("TMDB 국가 코드 API 호출 실패.", e);
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
    private TmdbMovieDetail fetchMovieDetail(Long movieId) {
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/movie/{movieId}")
                            .queryParam("language", "ko-KR")
                            .queryParam("region", "KR")
                            .build(movieId))
                    .retrieve()
                    .bodyToMono(TmdbMovieDetail.class)
                    .block();
        } catch (Exception e) {
            log.error("TMDB 개봉 날짜 API 호출 실패. movieId={}", movieId, e);
            throw new TmdbApiException("TMDB 영화 상세 API 호출 실패", e);
        }
    }

    // 크레딧(감독 및 출연진) api
    private TmdbCredit fetchCredit(Long movieId) {
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/movie/{movieId}/credits")
                            .queryParam("language", "ko-KR")
                            .build(movieId))
                    .retrieve()
                    .bodyToMono(TmdbCredit.class)
                    .block();
        } catch (Exception e) {
            throw new TmdbApiException("TMDB 크레딧 API 호출 실패", e);
        }
    }
}
