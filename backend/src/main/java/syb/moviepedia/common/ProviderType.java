package syb.moviepedia.common;

import lombok.Getter;

/**
 * 로그인 유형을 정의한다.
 */
@Getter
public enum ProviderType {
    LOCAL("LOCAL"), // 자체 로그인
    NAVER("NAVER"),
    GOOGLE("GOOGLE");

    private final String type;
    ProviderType(final String type) {
        this.type = type;
    }

    public static ProviderType from(String registrationId) {
        return ProviderType.valueOf(registrationId.toUpperCase());
    }
}
