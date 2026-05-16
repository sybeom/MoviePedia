package syb.moviepedia.comment.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import syb.moviepedia.comment.dto.CommentDto;

@Slf4j
@RestController("/movie/{movieId}")
public class CommentController {

    @PostMapping("/comments")
    public void saveComment(@PathVariable String movieId,
                            @RequestBody CommentDto dto) {
        log.info("saveComment 요청 도착");

    }
}
