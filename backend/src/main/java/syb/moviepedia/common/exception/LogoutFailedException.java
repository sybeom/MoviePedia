package syb.moviepedia.common.exception;

public class LogoutFailedException extends RuntimeException {

    public LogoutFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
