package syb.moviepedia.common.swagger;

import java.util.Set;

/**
 * api 실패 응답시 error를 함께 보내기 위한 swagger 명세 전용 클래스
 */
public record SwaggerFailResponse(
        String code,
        String message,
        Set<String> errors
) {
}
