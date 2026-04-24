package syb.moviepedia.common;

import lombok.Getter;

@Getter
public enum ProviderType {
    LOCAL("LOCAL"),
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
