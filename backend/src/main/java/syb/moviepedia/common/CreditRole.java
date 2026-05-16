package syb.moviepedia.common;

import lombok.Getter;

/**
 * 영화 크레딧의 역할
 */
@Getter
public enum CreditRole {
    DIRECTOR("Director"),
    ACTOR("Actor"),;

    private String role;
    CreditRole(String role) {
        this.role = role;
    }
}
