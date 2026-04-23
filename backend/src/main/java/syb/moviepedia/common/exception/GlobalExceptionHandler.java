package syb.moviepedia.common.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import syb.moviepedia.common.api.ApiResult;
import syb.moviepedia.common.api.ErrorCode;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 회원 가입 필드 중복 검사 예외
    @ExceptionHandler(DuplicateSignupFieldException.class)
    public ResponseEntity<ApiResult<Void>> handleSignupFieldException(DuplicateSignupFieldException e) {
        ErrorCode errorCode = ErrorCode.DUPLICATE_FIELD;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.fail(errorCode, e.getMessage(), e.getErrors()));
    }

    // @Valid 검증 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.fail(errorCode,"입력값 검증에 실패했습니다."));
    }
    // JwtService
}
