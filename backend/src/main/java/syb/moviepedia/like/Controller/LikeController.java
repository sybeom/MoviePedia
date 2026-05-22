package syb.moviepedia.like.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import syb.moviepedia.common.api.ApiResult;
import syb.moviepedia.like.service.LikeService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/movies/{movieId}/comments/{commentId}")
public class LikeController {

    private final LikeService likeService;

    /**
     * 좋아요를 단순 누르고, 취소하는 활성 비활성의 상태보다
     * 하나의 엔티티로 보아 저장, 삭제의 개념으로 보아야 각 코멘트의 좋아요 상태를 관리할 수 있다.
     */

    @PostMapping("/like")
    public ResponseEntity<ApiResult<Void>> saveLike(
            @PathVariable Long commentId,
            Authentication authentication) {

        // 요청 경로가 Security Config에 인가 설정되어 있으므로, 컨트롤러까지 도달했다는 것은
        // 액세스 토큰이 정상 인증 되었다는 뜻이고 파싱후 Authentication이 정상 생성되었다는 의미이므로 null 검사 X
        String loginId = authentication.getName();
        likeService.saveLike(commentId, loginId);

        return ResponseEntity.ok().body(ApiResult.success("좋아요 저장 성공"));
    }

    @DeleteMapping("/like")
    public ResponseEntity<ApiResult<Void>> deleteLike(
            @PathVariable Long commentId,
            Authentication authentication) {

        String loginId = authentication.getName();
        likeService.deleteLike(commentId, loginId);

        return ResponseEntity.ok().body(ApiResult.success("좋아요 삭제 성공"));
    }
}
