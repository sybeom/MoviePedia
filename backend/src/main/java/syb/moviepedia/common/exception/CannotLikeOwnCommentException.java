package syb.moviepedia.common.exception;

/**
 * 자신이 남긴 코멘트의 좋아요를 누를때 발생 예외
 */
public class CannotLikeOwnCommentException extends RuntimeException{
    public CannotLikeOwnCommentException(String message) {
        super(message);
    }
}
