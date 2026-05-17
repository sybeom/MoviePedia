package syb.moviepedia.common.exception;

/**
 * 한 영화에 코멘트를 이미 작성된 유저가 코멘트를 추가 작성하려고 할 때 발생
 * 즉 멤버당 1영화 1코멘트
 */
public class DuplicateCommentException extends RuntimeException {
    public DuplicateCommentException() {
    }
    public DuplicateCommentException(String message) {
        super(message);
    }
}
