package syb.moviepedia.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResult<List<CommentResponseDto>>> getComments(@PathVariable Long movieId) {
        return ResponseEntity.ok().body(ApiResult.success("코멘트 목록 조회 성공", commentService.getAllComments(movieId)));
    }

    @PostMapping("/comments")
    public ResponseEntity<FieldError> saveComment(
            @PathVariable Long movieId,
            @Valid @RequestBody CommentDto dto) { // 검증은 글로벌 예외에서 처리

        commentService.saveComment(movieId, dto);
        return ResponseEntity.ok().build();
    }
}
