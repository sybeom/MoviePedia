package syb.moviepedia.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import syb.moviepedia.comment.dto.CommentDto;
import syb.moviepedia.comment.service.CommentService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/movies/{movieId}")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/comments")
    public ResponseEntity<FieldError> saveComment(
            @PathVariable Long movieId,
            @Valid @RequestBody CommentDto dto) { // 검증은 글로벌 예외에서 처리

        commentService.saveComment(movieId, dto);
        return ResponseEntity.ok().build();
    }
}
