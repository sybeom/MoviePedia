package syb.moviepedia.tv.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import syb.moviepedia.common.MediaType;
import syb.moviepedia.common.SortType;
import syb.moviepedia.common.api.ApiSuccessResponse;
import syb.moviepedia.common.swagger.SwaggerApiResponse;
import syb.moviepedia.movie.dto.request.FilterRequest;
import syb.moviepedia.movie.dto.response.GenreResponse;
import syb.moviepedia.tv.dto.response.AllTVsResponse;
import syb.moviepedia.tv.dto.response.TVPopularResponse;
import syb.moviepedia.tv.dto.response.TVSeasonCreditResponse;
import syb.moviepedia.tv.dto.response.TVSeasonResponse;
import syb.moviepedia.tv.service.TVService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/tv")
public class TVController {
    private final TVService tvService;

    @Operation(
            summary = "TV 홈 TV 조회", description = "TV 탭의 필터링된 TV 목록 조회"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "필터링 TV 목록 조회 성공"),
            @ApiResponse(
                    responseCode = "502", description = "필터링 TV 목록 조회 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<ApiSuccessResponse<Slice<AllTVsResponse>>> getAllTVs(
            @PageableDefault(size = 10) Pageable pageable,
            @ModelAttribute FilterRequest filter,
            @RequestParam(defaultValue = "LATEST") SortType sort
    ) {
        return ResponseEntity.ok().body(ApiSuccessResponse.of("TV 목록 조회 성공" , tvService.getAllTV(filter, sort, pageable)));
    }

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

    @Operation(summary = "장르 목록", description = "필터 목록에 표시할 장르 목록을 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장르 데이터 조회 성공"),
            @ApiResponse(
                    responseCode = "502", description = "외부 TMDB API 호출 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            )
    })
    @GetMapping("/genres")
    public ResponseEntity<ApiSuccessResponse<List<GenreResponse>>> getGenres(@RequestParam MediaType mediaType) {
        List<GenreResponse> genres = tvService.getGenres(mediaType);
        log.info("genres: {}", genres);
        return ResponseEntity.ok().body(ApiSuccessResponse.of("장르 목록 조회 성공", genres));
    }

    @Operation(summary = "TV 시즌 상세", description = "TV 시리즈 시즌 상세 화면 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "TV 시리즈 시즌 상세화면 조회 성공"),
            @ApiResponse(
                    responseCode = "502", description = "외부 TMDB API 호출 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            )
    })
    @GetMapping("/{seriesCode}/{seasonNum}")
    public ResponseEntity<ApiSuccessResponse<TVSeasonResponse>> getTVSeasonDetail(
            @PathVariable Integer seriesCode, @PathVariable Integer seasonNum) {
        return ResponseEntity.ok().body(ApiSuccessResponse.of("TV 시즌 상세 조회 성공", tvService.getSeasonDetail(seriesCode, seasonNum)));
    }

    @Operation(summary = "TV 시즌 크레딧", description = "TV 시리즈 시즌 감독 및 출연 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "TV 시리즈 시즌 크레딧 조회 성공"),
            @ApiResponse(
                    responseCode = "502", description = "외부 TMDB APi 호출 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            )
    })
    @GetMapping("/{seriesCode}/{seasonNum}/credits")
    public ResponseEntity<ApiSuccessResponse<List<TVSeasonCreditResponse>>> getSeasonCredit(
            @PathVariable Integer seriesCode,
            @PathVariable Integer seasonNum
    ) {
        log.info("크레딧 컨트롤러 진입");
        return ResponseEntity.ok().body(ApiSuccessResponse.of(
                "TV 크레딧 조회 성공",
                tvService.getSeasonCredit(seriesCode, seasonNum)));
    }
}