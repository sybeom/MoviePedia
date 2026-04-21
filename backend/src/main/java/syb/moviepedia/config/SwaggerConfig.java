package syb.moviepedia.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Movie Pedia API Docs")
                        .description("Movie Pedia 프로젝트 API 문서입니다.")
                        .version("v1.0.0"))
                .servers(List.of(new Server() // API가 어떤 도메인을 통해 배포되고 있는지
                        .url("http://localhost:8080")
                        .description("개발용 로컬 서버")
                ));
    }
}
