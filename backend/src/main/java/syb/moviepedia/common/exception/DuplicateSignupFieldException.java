package syb.moviepedia.common.exception;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * 회원가입 필드 검증 실패시 발생하는 예외 클래스
 */
@Getter
public class DuplicateSignupFieldException extends RuntimeException {
    Set<String> errors;

    public DuplicateSignupFieldException(Set<String> errors, String message) {
        super(message);
        this.errors = errors;
    }
}
