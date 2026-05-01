package syb.moviepedia.movie.external;

import jakarta.persistence.Column;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * KMDB api 불러올 클래스
 */
@Component
@RequiredArgsConstructor
public class KmdbClient {
    private final WebClient kmdbWebClient;

    public String getPoster() {
        return kmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/openapi-data2/wisenut/search_api/search_json2.jsp")
                        .queryParam("collection", "kmdb_new2")
                        .queryParam("ServiceKey", "EFJ0G903EF2P3C42QKU0")
                        .queryParam("listCount", 100)
                        .queryParam("startCount", 0)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
