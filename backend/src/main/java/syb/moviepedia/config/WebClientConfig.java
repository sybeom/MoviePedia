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
                .baseUrl("https://www.kobis.or.kr/kobisopenapi/webservice/rest/movie")
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
            // 값은 RUN -> Edit Configuration -> Environment variables에 설정되어있음. Authorization 헤더에 설정된다
            @Value("${tmdb.api.token}") String token
    ) {
        System.out.println("TMDB baseUrl = " + baseUrl);
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .codecs(configurer -> // 메모리 제한 증가 256kb-> 2MB (아바타의 크레딧 응답 크기가 너무 커서 늘림)
                        configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)
                )
                .build();
    }
}
