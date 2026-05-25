package syb.moviepedia.common.swagger;

import java.util.List;

/**
 * 키워드 api 응답 성공 Swagger 클래스
 */
public record SwaggerKeywordListResponse(
        String code,
        String message,
        List<String> data
) {
}
