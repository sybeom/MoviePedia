package syb.moviepedia.common;

import lombok.Getter;

@Getter
public enum SocialProviderType {
    NAVER("NAVER"),
    GOOGLE("GOOGLE");

    private final String type;
        SocialProviderType(final String type) {
        this.type = type;
    }
}
