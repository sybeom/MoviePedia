package syb.moviepedia.common.api;

import lombok.Getter;

@Getter
public enum ErrorCode {
    VALIDATION_ERROR("VALIDATION_ERROR"),
    DUPLICATE_FIELD("DUPLICATE_FIELD");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }
}
