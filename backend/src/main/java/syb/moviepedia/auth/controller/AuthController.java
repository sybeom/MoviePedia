package syb.moviepedia.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syb.moviepedia.common.api.ApiResult;
import syb.moviepedia.common.swagger.SwaggerApiResponse;

@Tag(name = "Auth API", description = "인증 도메인 API")
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Operation(summary = "로그인 상태 판별", description = "토큰을 통해 현재 로그인 상태를 검증")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "토큰 인증 성공",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            )
    })
    @GetMapping("/me") // 홈 화면 진입시 로그인 상태 판별을 위해 토큰 검증을 하는 요청
    public ResponseEntity<ApiResult<Void>> checkMe() {
        return ResponseEntity.ok().body(ApiResult.success("토큰 인증에 성공하였습니다"));
    }
}
