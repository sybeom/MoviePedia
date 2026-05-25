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

    private final WebClient tmdbWebClient;
    private static final String DISCOVER_MOVIE_PATH = "/discover/movie";
    private static final String POPULAR_PATH = "/movie/popular";
    private static final String NOW_PLAYING = "/movie/now_playing";
    private static final String UPCOMING_PATH = "/movie/upcoming";
    private static final String GENRE_PATH = "/genre/movie/list";
    private static final String COUNTRY_PATH = "/configuration/countries";
    private static final String RELEASE_DATE_PATH = "/movie/{movieId}/release_dates";
    private static final String DETAIL_PATH = "/movie/{movieId}";
    private static final String CREDIT_PATH = "/movie/{movieId}/credits";
    private static final String SEARCH_MOVIE_LIST_PATH= "/search/movie";

    // 영화 검색어 목록
    public TmdbKeywordList getKeywordList(String keyword) {
        return fetchKeywordList(keyword);
    }

    // 전체 영화
    public TmdbMovieList getInitMovies() {
        return fetchInitMovies();
    }

    // 인기 영화
    public TmdbMovieList getPopularMovies() {
        return fetchCategoryMovieList(POPULAR_PATH);
    }

    // 현재 상영 영화
    public TmdbMovieList getNowPlayingMovies() {
        return fetchCategoryMovieList(NOW_PLAYING);
    }

    // 개봉 예정
    public TmdbMovieList getUpcomingMovies() {
        return fetchCategoryMovieList(UPCOMING_PATH);
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

    // 영화 검색어 목록 Api 리스트
    private TmdbKeywordList fetchKeywordList(String keyword) {
        try {
            return tmdbWebClient.get().uri(uriBuilder -> uriBuilder
                            .path(SEARCH_MOVIE_LIST_PATH)
                            .queryParam("query", keyword)
                            .queryParam("language", "ko-KR")
                            .build())
                    .retrieve()
                    .bodyToMono(TmdbKeywordList.class)
                    .block();
        } catch (Exception e) {
            throw new TmdbApiException("TMDB 영화 초기화 API 호출 실패. 경로= " + SEARCH_MOVIE_LIST_PATH, e);
        }
    }

    // 초기 영화 호출
    private TmdbMovieList fetchInitMovies() {
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                                    .path(DISCOVER_MOVIE_PATH)
                                    .queryParam("language", "ko-KR")
                                    .queryParam("page",1)
                                    .queryParam("region", "KR")
                                    .build())
                    .retrieve()
                    .bodyToMono(TmdbMovieList.class)
                    .block();
        } catch (Exception e) {
            throw new TmdbApiException("TMDB 영화 초기화 API 호출 실패. 경로= " + DISCOVER_MOVIE_PATH, e);
        }
    }

    // 카테고리 영화 목록 api 호출
    private TmdbMovieList fetchCategoryMovieList(String path) {
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
            throw new TmdbApiException("TMDB 영화 목록 API 호출 실패. 경로= " + path, e);
        }
    }

    // 장르 api 호출
    private TmdbGenreList fetchMovieGenres() {
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(GENRE_PATH)
                            .queryParam("language", "ko-KR")
                            .build())
                    .retrieve()
                    .bodyToMono(TmdbGenreList.class)
                    .block();
        } catch (Exception e) {
            throw new TmdbApiException("TMDB 장르 목록 API 호출 실패. 경로= " + GENRE_PATH, e);
        }
    }

    // 국가 코드 정보 API 호출
    private List<TmdbCountry> fetchCountries() {
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(COUNTRY_PATH)
                            .queryParam("language", "ko-KR")
                            .build())
                    .retrieve()
                    .bodyToFlux(TmdbCountry.class) // 응답의 최상위 구조가 객체가 아니라 배열이기때문에 List로 곧바로 받아야한다.
                    .collectList()
                    .block();
        } catch (Exception e) {
            throw new TmdbApiException("TMDB 국가 코드 API 호출 실패. 경로= " + COUNTRY_PATH, e);
        }
    }

    // 개봉일 api 호출 - 연령 등급 얻기(개봉일 api에서 영화 연령 등급을 얻을 수 있기때문)
    private TmdbMovieCertification fetchMovieReleaseDate(Long movieId) {
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(RELEASE_DATE_PATH)
                            .queryParam("language", "ko-KR")
                            .queryParam("region", "KR")
                            .build(movieId))
                    .retrieve()
                    .bodyToMono(TmdbMovieCertification.class)
                    .block();
        } catch (Exception e) {
            throw new TmdbApiException("TMDB 개봉 날짜 API 호출 실패. 경로= " + RELEASE_DATE_PATH, e);
        }
    }

    // 영화 상세 api 호출 (영화 상세는 변환할 데이터가 크게 없기 때문에 Dto 클래스로 받고 그대로 반환)
    private TmdbMovieDetail fetchMovieDetail(Long movieId) {
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(DETAIL_PATH)
                            .queryParam("language", "ko-KR")
                            .queryParam("region", "KR")
                            .build(movieId))
                    .retrieve()
                    .bodyToMono(TmdbMovieDetail.class)
                    .block();
        } catch (Exception e) {
            log.error("TMDB 개봉 날짜 API 호출 실패. movieId={}", movieId, e);
            throw new TmdbApiException("TMDB 영화 상세 API 호출 실패. 경로= " + DETAIL_PATH, e);
        }
    }

    // 크레딧(감독 및 출연진) api
    private TmdbCredit fetchCredit(Long movieId) {
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(CREDIT_PATH)
                            .queryParam("language", "ko-KR")
                            .build(movieId))
                    .retrieve()
                    .bodyToMono(TmdbCredit.class)
                    .block();
        } catch (Exception e) {
            throw new TmdbApiException("TMDB 크레딧 API 호출 실패. 경로 = " + CREDIT_PATH, e);
        }
    }
}
