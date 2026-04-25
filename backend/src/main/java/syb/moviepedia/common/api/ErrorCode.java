package syb.moviepedia.common.api;

import lombok.Getter;

@Getter
public enum ErrorCode {
    VALIDATION_ERROR("VALIDATION_ERROR"),
    DUPLICATE_FIELD("DUPLICATE_FIELD"),
    REFRESH_TOKEN_COOKIE_NOT_FOUND("REFRESH_TOKEN_COOKIE_NOT_FOUND"),
    INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }
}
