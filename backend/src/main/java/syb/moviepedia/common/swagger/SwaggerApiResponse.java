package syb.moviepedia.common.swagger;

/**
 * api 응답시 데이터 및 errors가 없는 경우 사용하는 응답 명세 전용 클래스 -> 성공,실패 모두 사용
 */
public record SwaggerApiResponse(
        String code,
        String message
) {
}
