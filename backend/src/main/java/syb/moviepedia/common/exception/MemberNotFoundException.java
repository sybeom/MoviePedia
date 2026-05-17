package syb.moviepedia.common.exception;

/**
 * 멤버가 DB에 존재하지 않는 경우
 */
public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(String message) {
        super(message);
    }
}
