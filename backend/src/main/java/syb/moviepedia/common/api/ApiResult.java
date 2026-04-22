package syb.moviepedia.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * api 응답 형식 통일하기 위한 클래스
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResult<T> (
        String code, // 실패 유형 코드
        String message,
        T data,
        Object errors) {

    public static <T> ApiResult<T> success() {
        return new ApiResult<>("SUCCESS", "요청에 성공했습니다.", null, null);
    }

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>("SUCCESS", "요청에 성공했습니다.", data, null);
    }

    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>("SUCCESS", message, data, null);
    }

    public static <T> ApiResult<T> fail(String code, String message) {
        return new ApiResult<>(code, message, null, null);
    }

    public static <T> ApiResult<T> fail(String code, String message, Object errors) {
        return new ApiResult<>(code, message, null, errors);
    }
}
