package syb.moviepedia.common.exception;

/**
 * 회원가입 필드 검증 실패시 발생하는 예외 클래스
 */
public class SignupFieldException extends RuntimeException {
    public SignupFieldException(String message) {
        super(message);
    }
}
