package syb.moviepedia.tv.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syb.moviepedia.common.api.ApiSuccessResponse;
import syb.moviepedia.common.swagger.SwaggerApiResponse;
import syb.moviepedia.tv.dto.response.TVPopularResponse;
import syb.moviepedia.tv.service.TVService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/tv")
public class TVController {
    private final TVService tvService;

    @Operation(
            summary = "TV 홈 인기 목록 조회", description = "TV 탭 홈 화면의 인기 TV 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "TV 인기 목록 조회 성공"),
            @ApiResponse(
                    responseCode = "502", description = "TV 인기 목록 조회 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            )
    })
    @GetMapping("/banners")
    public ResponseEntity<ApiSuccessResponse<List<TVPopularResponse>>> getPopularTVSeries() {
        log.info("/popular 호출 확인");
        return ResponseEntity.ok().body(
                ApiSuccessResponse.of("인기 TV 목록 조회 성공", tvService.getPopularTVList()));
    }
}
