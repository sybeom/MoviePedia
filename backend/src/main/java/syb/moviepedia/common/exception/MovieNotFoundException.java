package syb.moviepedia.common.exception;

/**
 * 조회할 영화가 DB 존재하지 않는 경우
 */
public class MovieNotFoundException extends RuntimeException {

    public MovieNotFoundException(String message) {
        super(message);
    }
}
