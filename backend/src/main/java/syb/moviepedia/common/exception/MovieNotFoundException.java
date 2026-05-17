package syb.moviepedia.common.exception;

/**
 * 조회할 영화가 DB에 없을 때
 */
public class MovieNotFoundException extends RuntimeException {

    public MovieNotFoundException(String message) {
        super(message);
    }
}
