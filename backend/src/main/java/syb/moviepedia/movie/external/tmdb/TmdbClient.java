package syb.moviepedia.movie.external.tmdb;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * TMDB API 호출하는 클라이언트 클래스
 */
@Component
@RequiredArgsConstructor
public class TmdbClient {

    private final WebClient tmdbWebClient;

    public String getPopularMovies() {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/popular")
                        .queryParam("language", "ko-KR")
                        .queryParam("page", 1)
                        .queryParam("region", "KR")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
