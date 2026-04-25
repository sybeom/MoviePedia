package syb.moviepedia.common.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
