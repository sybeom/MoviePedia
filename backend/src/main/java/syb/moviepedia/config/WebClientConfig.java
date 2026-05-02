package syb.moviepedia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 외부 서버와 통신하기 위한 WebClient 설정 클래스
 * 외부 API를 호출하기 위해 HTTP 요청을 보낼 때 사용
 */
@Configuration
public class WebClientConfig {
    @Value("${tmdb.api.base-url}")
    private String tmdbBaseUrl;

    @Value("${tmdb.api.token}")
    private String tmdbToken;

    @Bean // 영화진흥위원회
    public WebClient kobisWebClient() {
        return WebClient.builder()
                .baseUrl("https://www.kobis.or.kr/kobisopenapi/webservice/rest")
                .build();
    }

    @Bean // KMDb
    public WebClient kmdbWebClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer ->
                        configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)
                )
                .build();

        return WebClient.builder()
                .baseUrl("https://api.koreafilm.or.kr")
                .exchangeStrategies(strategies)
                .build();
    }

    @Bean // TMDB
    public WebClient tmdbWebClient(
            @Value("${tmdb.api.base-url}") String baseUrl,
            @Value("${tmdb.api.token}") String token
    ) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }
}
