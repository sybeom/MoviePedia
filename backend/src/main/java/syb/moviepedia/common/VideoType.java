package syb.moviepedia.common;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum VideoType {
    TRAILER("Trailer"),
    TEASER("Teaser"),
    UNKNOWN("Unknown");

    private final String value;

    VideoType(String value) {
        this.value = value;
    }

    // Tmdb Video api의 응답에서 type 값이 TEASER가아닌 Teaser처럼 들어오기때문에 변환이 필요함
    @JsonCreator
    public static VideoType from(String value) {
        for (VideoType type : values()) { // values()는 enum 상수들을 배열로 반환
            if (type.value.equals(value)) { // 상수의 value 필드가 파라미터의 value와 일치하면 상수(type) 그대로 반환
                return type;
            }
        }
        return UNKNOWN;
    }
}
