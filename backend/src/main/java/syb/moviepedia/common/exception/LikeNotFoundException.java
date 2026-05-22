package syb.moviepedia.common.exception;

/**
 * 좋아요를 찾지 못했을 경우 발생(좋아요 취소(삭제) 시)
 */
public class LikeNotFoundException extends RuntimeException{
    public LikeNotFoundException(String message) {
        super(message);
    }
}
