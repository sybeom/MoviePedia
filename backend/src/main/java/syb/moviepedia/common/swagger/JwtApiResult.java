package syb.moviepedia.common.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import syb.moviepedia.jwt.dto.JwtResponseDto;

/**
 * 현재 API 응답은 공통으로 ApiResult<T> 형식으로 응답된다.
 * 하지만 Swagger 명세 작성에서 응답은 ApiResult<T> 형식으로 작성할 수 없다.
 * 따라서 응답 명세 작성을 할 수 있도록 Swagger 응답 명세를 위한 전용 클래스를 만든다.
 * 즉, 순전 Swagger에 응답 명세를 기록하기 위한 클래스이다.
 */
@Schema(name = "JwtApiResult") // swagger-ui 문서에 표기할 명칭
public record JwtApiResult(
        String code,
        String message,
        JwtResponseDto data) {
}
