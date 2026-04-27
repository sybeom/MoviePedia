package syb.moviepedia.common;

import lombok.Getter;

public enum RoleType {
    USER("USER"),
    ADMIN("ADMIN");

    private final String role;

    RoleType(String role) {
        this.role = role;
    }
}
