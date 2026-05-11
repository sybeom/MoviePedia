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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syb.moviepedia.common.api.ApiResult;
import syb.moviepedia.common.swagger.SwaggerApiResponse;
import syb.moviepedia.common.swagger.SwaggerMovieCategoriesDtoResponse;
import syb.moviepedia.movie.dto.MovieCategoriesDto;
import syb.moviepedia.movie.dto.MovieDetailDto;
import syb.moviepedia.movie.service.MovieService;

@Tag(name = "Movie API", description = "영화 도메인 API")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/movies")
public class MovieController {
    private final MovieService movieService;

    @Operation(
            summary = "카테고리 별 영화 목록",
            description = "홈 화면 카테고리 별 영화 목록을 가져온다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    schema = @Schema(implementation = SwaggerMovieCategoriesDtoResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "502",
                            description = "실패",
                            content = @Content(
                                    schema = @Schema(implementation = SwaggerApiResponse.class)
                            )
                    )
            }
    )
    @GetMapping
    public ResponseEntity<ApiResult<MovieCategoriesDto>> home() {
        return ResponseEntity.ok().body(ApiResult.success("카테고리 별 영화 목록 조회 성공",movieService.getCategoryMovies()));
    }

    @Operation(
            summary = "영화 상세",
            description = "영화 상세 정보를 응답한다"
    )
    @GetMapping("/{movieId}")
    public ResponseEntity<ApiResult<MovieDetailDto>> getMovieDetail(@PathVariable Long movieId) {
        return ResponseEntity.ok().body(ApiResult.success("영화 상세 정보",movieService.getMovieDetail(movieId)));
    }
}
