package syb.moviepedia.common.api;

public record ApiFailResponse (
        String code,
        String message,
        Object errors
) {
    public static ApiFailResponse of(ErrorCode errorCode, String message, Object errors) {
        return new ApiFailResponse(errorCode.name(), message, errors);
    }

    public static ApiFailResponse of(ErrorCode errorCode, String message) {
        return new ApiFailResponse(errorCode.name(), message, null);
    }
}
