package syb.moviepedia.common.exception;

/**
 * 소셜 로그인에서 보내는 쿠키가 존재하지 않을때.
 */
public class RefreshTokenCookieNotFoundException extends RuntimeException {
    public RefreshTokenCookieNotFoundException(String message) {
        super(message);
    }
}
