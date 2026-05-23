package syb.moviepedia.movie.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syb.moviepedia.common.api.ApiResult;
import syb.moviepedia.common.swagger.SwaggerApiResponse;
import syb.moviepedia.movie.dto.response.MovieDetailResponse;
import syb.moviepedia.movie.dto.response.MovieCategoriesResponse;
import syb.moviepedia.movie.service.MovieService;

@Tag(name = "Movie API", description = "영화 도메인 API")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/movies")
public class MovieController {
    private final MovieService movieService;

    @Operation(
            summary = "카테고리 별 영화 목록", description = "홈 화면 카테고리 별 영화 목록을 가져온다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "카테고리 영화 목록 조회 성공"),
            @ApiResponse(
                    responseCode = "502", description = "외부 TMDB API 호출 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<ApiResult<MovieCategoriesResponse>> home() {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success("카테고리 별 영화 목록 조회 성공", movieService.getCategoryMovies()));
    }

    @Operation(summary = "영화 상세", description = "영화 상세 정보를 응답한다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "영화 상세 정보 조회 성공"),
            @ApiResponse(
                    responseCode = "502", description = "외부 TMDB API 호출 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            )
    })
    @GetMapping("/{movieCode}")
    public ResponseEntity<ApiResult<MovieDetailResponse>> getMovieDetail(@PathVariable Long movieCode) {
        return ResponseEntity.ok().body(ApiResult.success("영화 상세 정보",movieService.getMovieDetail(movieCode)));
    }
}
