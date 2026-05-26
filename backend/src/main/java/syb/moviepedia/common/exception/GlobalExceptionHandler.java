package syb.moviepedia.common.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import syb.moviepedia.common.api.ApiFailResponse;
import syb.moviepedia.common.api.ErrorCode;

import java.util.HashSet;
import java.util.Set;

/**
 * 컨트롤러까지 도달한 경우의 예외를 처리한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 회원 가입 필드 중복 검사 예외
    @ExceptionHandler(DuplicateSignupFieldException.class)
    public ResponseEntity<ApiFailResponse> handleSignupFieldException(DuplicateSignupFieldException e) {
        return fail(ErrorCode.DUPLICATE_FIELD, HttpStatus.CONFLICT, e.getMessage(), e.getErrors());
    }

    // @Valid 검증 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiFailResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Set<String> errors = new HashSet<>();
        if (e.hasErrors()) {
            e.getFieldErrors().forEach(fieldError -> {
                errors.add(fieldError.getField());
            });
        }
        log.error("로그인 에러 :", e);
        return fail(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "입력값 검증 실패", errors);
    }

    // 소셜 로그인 쿠키 존재 X 예외
    @ExceptionHandler(RefreshTokenCookieNotFoundException.class)
    public ResponseEntity<ApiFailResponse> handleCookieNotFoundException(RefreshTokenCookieNotFoundException e) {
        return fail(ErrorCode.REFRESH_TOKEN_COOKIE_NOT_FOUND, HttpStatus.UNAUTHORIZED, e);
    }

    // 유효하지 않은 리프레쉬 토큰
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiFailResponse> handleInvalidRefreshTokenException(InvalidRefreshTokenException e) {
        return fail(ErrorCode.INVALID_REFRESH_TOKEN, HttpStatus.UNAUTHORIZED, e);
    }

    // 로그아웃 실패
    @ExceptionHandler(LogoutFailedException.class)
    public ResponseEntity<ApiFailResponse> handleLogoutFailedException(LogoutFailedException e) {
        return fail(ErrorCode.LOGOUT_FAILED, HttpStatus.BAD_REQUEST, e);
    }

    // Json 파싱 실패
    @ExceptionHandler(JsonParsingFailedException.class)
    public ResponseEntity<ApiFailResponse> handleJsonParsingFailedException(JsonParsingFailedException e) {
        return fail(ErrorCode.LOGOUT_FAILED, HttpStatus.BAD_REQUEST, e);
    }

    // TMDB API 호출 실패 에러 (API 실패 에러는 서버 문제보다는 API 문제이기에 502 상태코드를 전송한다.)
    @ExceptionHandler(TmdbApiException.class)
    public ResponseEntity<ApiFailResponse> handleTmdbApiException(TmdbApiException e) {
        log.error("TMDB API 호출 실패", e);
        return fail(ErrorCode.TMDB_API_FAILED, HttpStatus.BAD_GATEWAY, e);
    }

    // 영화 코드 조회 실패
    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<ApiFailResponse> handleMovieNotFoundException(MovieNotFoundException e) {
        return fail(ErrorCode.MOVIE_NOT_FOUND, HttpStatus.NOT_FOUND, e);
    }

    // 멤버 조회 실패
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ApiFailResponse> handleMemberNotFoundException(MemberNotFoundException e) {
        return fail(ErrorCode.MEMBER_NOT_FOUND, HttpStatus.NOT_FOUND, e);
    }

    // 코멘트 재작성 시도
    @ExceptionHandler(CommentAlreadyExistsException.class)
    public ResponseEntity<ApiFailResponse> handleCommentAlreadyExistsException(CommentAlreadyExistsException e) {
        return fail(ErrorCode.ALREADY_EXISTS_COMMENT, HttpStatus.CONFLICT, e);
    }

    // 코멘트 조회 실패
    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ApiFailResponse> handleCommentNotFoundException(CommentNotFoundException e) {
        return fail(ErrorCode.COMMENT_NOT_FOUND, HttpStatus.NOT_FOUND, e);
    }

    // 코멘트 작성 멤버 조회 실패
    @ExceptionHandler(CommentMemberNotFound.class)
    public ResponseEntity<ApiFailResponse> handleCommentMemberNotFound(CommentMemberNotFound e) {
        return fail(ErrorCode.COMMENT_MEMBER_NOT_FOUND, HttpStatus.NOT_FOUND, e);
    }

    // 좋아요 중복 클릭
    @ExceptionHandler(AlreadyLikedException.class)
    public ResponseEntity<ApiFailResponse> handleDuplicateLikeException(AlreadyLikedException e) {
        return fail(ErrorCode.ALREADY_LIKED, HttpStatus.CONFLICT, e);
    }

    // 자신이 작성한 코멘트에 좋아요 클릭
    @ExceptionHandler(CannotLikeOwnCommentException.class)
    public ResponseEntity<ApiFailResponse> handleCannotLikeOwnCommentException(CannotLikeOwnCommentException e) {
        return fail(ErrorCode.CANNOT_LIKE_OWN_COMMENT, HttpStatus.FORBIDDEN, e);
    }

    // 좋아요 취소 클릭시 해당 좋아요 찾지 못했을 경우
    @ExceptionHandler(LikeNotFoundException.class)
    public ResponseEntity<ApiFailResponse> handleLikeNotFoundException(LikeNotFoundException e) {
        return fail(ErrorCode.LIKE_NOT_FOUND, HttpStatus.NOT_FOUND, e);
    }

    // 실패 응답 생성 - errors 없는 경우 (에러코드와 메시지만 전송)
    private ResponseEntity<ApiFailResponse> fail(
            ErrorCode errorCode,
            HttpStatus status,
            Exception e
    ) {
        return ResponseEntity
                .status(status)
                .body(ApiFailResponse.of(errorCode, e.getMessage()));
    }

    // 실패 응답 생성 - errors 있는 경우
    private ResponseEntity<ApiFailResponse> fail(
            ErrorCode errorCode,
            HttpStatus status,
            String message,
            Object errors
    ) {
        return ResponseEntity
                .status(status)
                .body(ApiFailResponse.of(errorCode, message, errors));
    }
}
