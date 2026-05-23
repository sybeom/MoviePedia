package syb.moviepedia.like.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import syb.moviepedia.common.api.ApiResult;
import syb.moviepedia.common.swagger.SwaggerApiResponse;
import syb.moviepedia.common.swagger.SwaggerFailResponse;
import syb.moviepedia.like.service.LikeService;

@Tag(name = "Like API", description = "좋아요 도메인 API")
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

    @Operation(summary = "좋아요 클릭", description = "좋아요 클릭시 좋아요를 생성한다")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201", description = "좋아요 생성 성공",
                    content =  @Content(
                            schema =  @Schema(implementation = SwaggerApiResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403", description = "자신의 코멘트에 좋아요 클릭",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )),
            @ApiResponse(
                    responseCode = "409", description = "이미 해당 코멘트에 좋아요 클릭",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            )
    })
    @PostMapping("/like")
    public ResponseEntity<ApiResult<Void>> saveLike(
            @PathVariable Long commentId,
            Authentication authentication) {

        // 요청 경로가 Security Config에 인가 설정되어 있으므로, 컨트롤러까지 도달했다는 것은
        // 액세스 토큰이 정상 인증 되었다는 뜻이고 파싱후 Authentication이 정상 생성되었다는 의미이므로 null 검사 X
        String loginId = authentication.getName();
        likeService.saveLike(commentId, loginId);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success("좋아요 저장 성공"));
    }

    @Operation(summary = "좋아요 취소", description = "좋아요를 취소 클릭에 의해 삭제한다")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "좋아요 삭제 성공",
                    content =  @Content(
                            schema =  @Schema(implementation = SwaggerApiResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "좋아요 찾지 못함",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            )
    })
    @DeleteMapping("/like")
    public ResponseEntity<ApiResult<Void>> deleteLike(
            @PathVariable Long commentId,
            Authentication authentication) {

        String loginId = authentication.getName();
        likeService.deleteLike(commentId, loginId);

        return ResponseEntity.ok().body(ApiResult.success("좋아요 삭제 성공"));
    }
}
