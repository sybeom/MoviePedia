package syb.moviepedia.movie.external.tmdb;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import syb.moviepedia.movie.external.tmdb.dto.TmdbGenreListResponse;
import syb.moviepedia.movie.external.tmdb.dto.TmdbPopularMovieListResponse;

/**
 * TMDB API 호출하는 클라이언트 클래스
 * 인기 영화 목록 응답에서 장르가 이름이 아닌 번호로 오기때문에 장르 번호와 장르명을 응답 받는 장르 API를 별도 호출한다
 */
@Component
@RequiredArgsConstructor
public class TmdbClient {

    private final WebClient tmdbWebClient;

    // 인기 영화 목록
    public TmdbPopularMovieListResponse getPopularMovies() {
        return fetchPopularMovies(); // api 호출
    }

    // 영화 장르 정보
    public TmdbGenreListResponse getMovieGenres() {
        return fetchMovieGenres(); // api 호출
    }

    // tmdb api 호출
    public TmdbPopularMovieListResponse fetchPopularMovies() {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/popular")
                        .queryParam("language", "ko-KR")
                        .queryParam("page", 1)
                        .queryParam("region", "KR") // 한국 지역 기준으로 개봉일, 공개 여부, 결과 순서 등을 맞춘다.
                        .build())
                .retrieve()
                .bodyToMono(TmdbPopularMovieListResponse .class) // 응답 json에서 해당 필드에 맞게 데이터가 자동 매핑된다.
                .block();
    }

    // 장르 api 호출
    public TmdbGenreListResponse fetchMovieGenres() {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/genre/movie/list")
                        .queryParam("language", "ko-KR")
                        .build())
                .retrieve()
                .bodyToMono(TmdbGenreListResponse.class)
                .block();
    }
}
