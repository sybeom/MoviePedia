package syb.moviepedia.movie.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syb.moviepedia.common.api.ApiResult;
import syb.moviepedia.common.swagger.SwaggerTmdbMovieSummaryListResponse;
import syb.moviepedia.movie.dto.TmdbMovieSummaryDto;
import syb.moviepedia.movie.service.MovieService;

import java.util.List;

@Tag(name = "Movie API", description = "영화 도메인 API")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/movies")
public class MovieController {
    private final MovieService movieService;

    @Operation(
            summary = "인기 영화 목록",
            description = "메인 화면 인기 영화 목록 요청 API이다",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    schema = @Schema(implementation = SwaggerTmdbMovieSummaryListResponse.class)
                            )
                    )
            }
    )
    @GetMapping("/popular")
    public ResponseEntity<ApiResult<List<TmdbMovieSummaryDto>>> getPopularMovies() {
        return ResponseEntity.ok().body(ApiResult.success("TMDB 인기 영화 목록", movieService.getPopularMovies()));
    }

    @Operation(
            summary = "현재 상영 영화 목록",
            description = "메인 화면 현재 상영 영화 목록 요청 API이다",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    schema = @Schema(implementation = SwaggerTmdbMovieSummaryListResponse.class)
                            )
                    )
            }
    )
    @GetMapping("/now_playing")
    public ResponseEntity<ApiResult<List<TmdbMovieSummaryDto>>> getNowPlayingMovies() {
        return ResponseEntity.ok().body(ApiResult.success("TMDB 현재 상영 중인 영화", movieService.getNowPlayingMovies()));
    }

    @Operation(
            summary = "개봉 예정 영화 목록",
            description = "메인 화면 개봉예정 영화 목록 요청 API이다",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    schema = @Schema(implementation = SwaggerTmdbMovieSummaryListResponse.class)
                            )
                    )
            }
    )
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResult<List<TmdbMovieSummaryDto>>> getUpcomingMovies() {
        return ResponseEntity.ok().body(ApiResult.success("TMDB 개봉 예정작", movieService.getUpcomingMovies()));
    }
}
