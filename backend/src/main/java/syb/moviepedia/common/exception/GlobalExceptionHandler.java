package syb.moviepedia.common.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import syb.moviepedia.common.api.ApiResult;
import syb.moviepedia.common.api.ErrorCode;

import java.util.HashSet;
import java.util.Set;

/**
 * 컨트롤러까지 도달한 경우의 예외를 처리한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //TODO: 응답 생성 메서드 고려해보기, 모두 공통이라 메서드화 해도 될듯.

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
        Set<String> errors = new HashSet<>();
        if (e.hasErrors()) {
            e.getFieldErrors().forEach(fieldError -> {
                errors.add(fieldError.getField());
            });
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.fail(errorCode, "입력값 검증 실패", errors));
    }

    // 소셜 로그인 쿠키 존재 X 예외
    @ExceptionHandler(RefreshTokenCookieNotFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleCookieNotFoundException(RefreshTokenCookieNotFoundException e) {
        ErrorCode errorCode = ErrorCode.REFRESH_TOKEN_COOKIE_NOT_FOUND;
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResult.fail(errorCode, e.getMessage()));
    }

    // 유효하지 않은 리프레쉬 토큰
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiResult<Void>> handleInvalidRefreshTokenException(InvalidRefreshTokenException e) {
        ErrorCode errorCode = ErrorCode.INVALID_REFRESH_TOKEN;
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResult.fail(errorCode, e.getMessage()));
    }

    @ExceptionHandler(LogoutFailedException.class)
    public ResponseEntity<ApiResult<Void>> handleLogoutFailedException(LogoutFailedException e) {
        ErrorCode errorCode = ErrorCode.LOGOUT_FAILED;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.fail(errorCode, e.getMessage()));
    }

    // Json 파싱 실패
    @ExceptionHandler(JsonParsingFailedException.class)
    public ResponseEntity<ApiResult<Void>> handleJsonParsingFailedException(JsonParsingFailedException e) {
        ErrorCode errorCode = ErrorCode.JSON_PARSING_FAILED;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.fail(errorCode, e.getMessage()));
    }

    @ExceptionHandler(TmdbApiException.class)
    public ResponseEntity<ApiResult<Void>> handleTmdbApiException(TmdbApiException e) {
        ErrorCode errorCode = ErrorCode.TMDB_API_FAILED;
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ApiResult.fail(errorCode, e.getMessage()));
    }
}
