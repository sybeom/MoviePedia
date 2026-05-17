package syb.moviepedia.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void saveComment(@PathVariable Long movieId,
                            @RequestBody CommentDto dto) {
        log.info("saveComment 요청 도착");

        commentService.saveComment(movieId, dto);
    }
}
