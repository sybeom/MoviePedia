package syb.moviepedia.like.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syb.moviepedia.like.service.LikeService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/movies/{movieId}/comments/{commentId}")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/like")
    public void saveLike(@PathVariable Long commentId) {
        likeService.saveLike(commentId);
    }
}
