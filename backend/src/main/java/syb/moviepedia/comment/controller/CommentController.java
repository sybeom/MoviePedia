package syb.moviepedia.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import syb.moviepedia.comment.dto.CommentDto;
import syb.moviepedia.comment.dto.CommentResponseDto;
import syb.moviepedia.comment.service.CommentService;
import syb.moviepedia.common.api.ApiResult;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/movies/{movieId}")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/comments")
    public ResponseEntity<ApiResult<List<CommentResponseDto>>> getComments(
            @PathVariable Long movieId,
            Authentication authentication) {

        // 작성자 코멘트를 찾기 위한 로그인 판별. 아이디가 있으면 로그인 상태, null이면 비로그인
        // 로그인 아이디를 바탕으로 작성자 코멘트를 찾는다.
        String loinId = authentication != null ? authentication.getName() : null;

        return ResponseEntity.ok().body(ApiResult.success("코멘트 목록 조회 성공", commentService.getAllComments(movieId, loinId)));
    }

    @PostMapping("/comments")
    public ResponseEntity<FieldError> saveComment(
            @PathVariable Long movieId,
            @Valid @RequestBody CommentDto dto) { // 검증은 글로벌 예외에서 처리

        commentService.saveComment(movieId, dto);
        return ResponseEntity.ok().build();
    }
}
