package syb.moviepedia.like.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
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
    public void saveLike(
            @PathVariable Long commentId,
            Authentication authentication) {
        // 요청 경로가 Security Config에 인가 설정되어 있으므로, 컨트롤러까지 도달했다는 것은
        // 액세스 토큰이 정상 인증 되었다는 뜻이고 파싱후 Authentication이 정상 생성되었다는 의미이므로 null 검사 X
        String loginId = authentication.getName();
        likeService.saveLike(commentId, loginId);
    }
}
