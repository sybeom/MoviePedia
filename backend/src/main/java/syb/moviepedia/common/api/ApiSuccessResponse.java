package syb.moviepedia.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiSuccessResponse<T>(
        String code,
        String message,
        T data
) {
    public static <T> ApiSuccessResponse<T> of(String message) {
        return new ApiSuccessResponse<>("SUCCESS", message, null);
    }
    public static <T> ApiSuccessResponse<T> of(String message, T data) {
        return new ApiSuccessResponse<>("SUCCESS", message, data);
    }
}
