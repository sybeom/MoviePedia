package syb.moviepedia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 외부 서버와 통신하기 위한 WebClient 설정 클래스
 * 외부 API를 호출하기 위해 HTTP 요청을 보낼 때 사용
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://api.koreafilm.or.kr")
                .build();
    }
}
