package syb.moviepedia.movie.external.tmdb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import syb.moviepedia.common.exception.TmdbApiException;
import syb.moviepedia.movie.external.tmdb.dto.*;

import java.util.List;
import java.util.function.Consumer;

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
    private static final String RELEASE_DATE_PATH = "/movie/{movieCode}/release_dates";
    private static final String DETAIL_PATH = "/movie/{movieCode}";
    private static final String CREDIT_PATH = "/movie/{movieCode}/credits";
    private static final String TRAILER_PATH = "/movie/{movieCode}/videos";

    // 전체 영화
    public TmdbMovieList getInitMovies(int page) {
        return fetchInitMovies(page);
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
    public TmdbMovieCertification getMovieCertification(Integer mvCode) {
        // 개봉일 api로 관람등급을 얻는다.
        return fetchMovieReleaseDate(mvCode);
    }

    // 국가 정보
    public List<TmdbCountry> getCountries() {
        return fetchCountries();
    }

    // 영화 상세
    public TmdbMovieDetail getMovieDetail(Integer mvCode) {
        return fetchMovieDetail(mvCode);
    }

    // 크레딧 (감독, 출연) 정보
    public TmdbCredit getCredit(Integer mvCode) {
        return fetchCredit(mvCode);
    }

    // 비디오(트레일러) 정보
    public TmdbVideoResponse getVideos(Integer mvCode) {
        return fetchMovieVideos(mvCode);
    }

    // 초기 영화 호출
    private TmdbMovieList fetchInitMovies(int page) {
        return get(
                DISCOVER_MOVIE_PATH,
                TmdbMovieList.class,
                "TMDB 영화 초기화 API 호출 실패",
                uriBuilder -> uriBuilder
                        .queryParam("language", "ko-KR")
                        .queryParam("page",page)
                        .queryParam("region", "KR")
        );
    }

    // 카테고리 영화 목록 api 호출
    private TmdbMovieList fetchCategoryMovieList(String path) {
        return get(
                path,
                TmdbMovieList.class,
                "TMDB 영화 목록 API 호출 실패",
                uriBuilder -> uriBuilder
                        .queryParam("language", "ko-KR")
                        .queryParam("page", 1)
                        .queryParam("region", "KR") // 한국 지역 기준으로 개봉일, 공개 여부, 결과 순서 등을 맞춘다.
        );
    }

    // 장르 api 호출
    private TmdbGenreList fetchMovieGenres() {
        return get(
                GENRE_PATH,
                TmdbGenreList.class,
                "TMDB 장르 목록 API 호출 실패",
                uriBuilder -> uriBuilder
                        .queryParam("language", "ko-KR")
        );
    }

    // 국가 코드 정보 API 호출
    private List<TmdbCountry> fetchCountries() {
        return getList(
                COUNTRY_PATH,
                TmdbCountry.class,
                "TMDB 국가 코드 API 호출 실패",
                uriBuilder -> uriBuilder
                        .queryParam("language", "ko-KR")
        );
    }

    // 개봉일 api 호출 - 연령 등급 얻기(개봉일 api에서 영화 연령 등급을 얻을 수 있기때문)
    private TmdbMovieCertification fetchMovieReleaseDate(Integer mvCode) {
        return get(
                RELEASE_DATE_PATH,
                TmdbMovieCertification.class,
                "TMDB 개봉 날짜 API 호출 실패",
                uriBuilder -> uriBuilder
                        .queryParam("language", "ko-KR")
                        .queryParam("region", "KR"),
                mvCode
        );
    }

    // 영화 상세 api 호출 (영화 상세는 변환할 데이터가 크게 없기 때문에 Dto 클래스로 받고 그대로 반환)
    private TmdbMovieDetail fetchMovieDetail(Integer mvCode) {
        return get(
                DETAIL_PATH,
                TmdbMovieDetail.class,
                "TMDB 영화 상세 API 호출 실패",
                uriBuilder -> uriBuilder
                        .queryParam("language", "ko-KR")
                        .queryParam("region", "KR"),
                mvCode
        );
    }

    // 크레딧(감독 및 출연진) api
    private TmdbCredit fetchCredit(Integer mvCode) {
        return get(
                CREDIT_PATH,
                TmdbCredit.class,
                "TMDB 크레딧 API 호출 실패",
                uriBuilder -> uriBuilder
                        .queryParam("language", "ko-KR"),
                mvCode
        );
    }

    // 영화 트레일러 api
    private TmdbVideoResponse fetchMovieVideos(Integer mvCode) {
        return get(
                TRAILER_PATH,
                TmdbVideoResponse.class,
                "TMDB 트레일러 API 호출 실패",
                uriBuilder -> uriBuilder
                        .queryParam("language", "ko-KR"),
                mvCode
        );
    }

    // api 호출 메서드 공통화
    private <T> T get(
            String path,
            Class<T> responseType,
            String errorMessage,
            Consumer<UriBuilder> queryParams,
            Object... uriVariables // movieId가 없는 경우도 있으므로 가변 인자로 설정
    ) {
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path(path);

                        if (queryParams != null) {
                            queryParams.accept(uriBuilder);
                        }

                        return uriBuilder.build(uriVariables);
                    })
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();

        } catch (Exception e) {
            throw new TmdbApiException(errorMessage + ". 경로= " + path, e);
        }
    }

    // api 호출 메서드 공통화(응답이 배열일 경우)
    private <T> List<T> getList(
            String path,
            Class<T> elementType,
            String errorMessage,
            Consumer<UriBuilder> queryParams,
            Object... uriVariables
    ) {
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path(path);

                        if (queryParams != null) {
                            queryParams.accept(uriBuilder);
                        }

                        return uriBuilder.build(uriVariables);
                    })
                    .retrieve()
                    .bodyToFlux(elementType)
                    .collectList()
                    .block();

        } catch (Exception e) {
            throw new TmdbApiException(errorMessage + ". 경로= " + path, e);
        }
    }
}
