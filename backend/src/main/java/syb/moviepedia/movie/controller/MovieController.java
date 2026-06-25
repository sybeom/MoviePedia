package syb.moviepedia.movie.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import syb.moviepedia.common.SortType;
import syb.moviepedia.common.api.ApiSuccessResponse;
import syb.moviepedia.common.swagger.SwaggerApiResponse;
import syb.moviepedia.movie.dto.request.FilterRequest;
import syb.moviepedia.movie.dto.response.*;
import syb.moviepedia.movie.service.MovieService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/movies")
public class MovieController {
    private final MovieService movieService;

    @Operation(
            summary = "홈 화면 전체 영화", description = "홈 화면 전체 영화 목록 10개씩 가져온다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "전체 영화 목록 조회 성공"),
            @ApiResponse(
                    responseCode = "502", description = "전체 영화 목록 조회 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<ApiSuccessResponse<Slice<AllMoviesResponse>>> allMovies(
            @PageableDefault(size = 10) Pageable pageable,
            @ModelAttribute FilterRequest filter,
            @RequestParam(defaultValue = "LATEST") SortType sort) {
        log.info("필터 목록: {}, 개봉 상태 : {}",filter.toString(), filter.releaseStatus());
        return ResponseEntity.ok().body(ApiSuccessResponse.of(
                "전체 영화 목록 조회 성공", movieService.getAllMovies(filter, sort, pageable)));
    }

    @Operation(
            summary = "홈화면 배너 영화", description = "홈 화면 배너에 보여질 인기영화 목록의 back_drop 경로를 가져온다")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "배너 영화 목록 조회 성공"),
            @ApiResponse(
                    responseCode = "502", description = "외부 TMDB API 호출 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            )
    })
    @GetMapping("/banners")
    public ResponseEntity<ApiSuccessResponse<List<MovieBannerResponse>>> getBannerMovies() {
        return ResponseEntity.ok().body(ApiSuccessResponse.of("영화 배너 목록 조회 성공", movieService.getBannerMovies()));
    }

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
    @GetMapping("/categories")
    public ResponseEntity<ApiSuccessResponse<MovieCategoriesResponse>> getCategoryMovies() {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccessResponse.of("카테고리 별 영화 목록 조회 성공", movieService.getCategoryMovies()));
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
    public ResponseEntity<ApiSuccessResponse<List<GenreResponse>>> getGenres() {
        List<GenreResponse> genres = movieService.getGenres();
        log.info("genres: {}", genres);
        return ResponseEntity.ok().body(ApiSuccessResponse.of("장르 목록 조회 성공", genres));
    }

    @Operation(summary = "영화 상세", description = "영화 상세 페이지 데이터를 조회한다")
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
    public ResponseEntity<ApiSuccessResponse<MovieDetailResponse>> getMovieDetail(@PathVariable Integer movieCode) {
        return ResponseEntity.ok().body(ApiSuccessResponse.of("영화 상세 정보", movieService.getMovieDetail(movieCode)));
    }

    @Operation(summary = "영화 트레일러", description = "영화 트레일러를 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "영화 트레일러 조회 성공"),
            @ApiResponse(responseCode = "502", description = "외부 TMDB API 호출 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    ))
    })
    @GetMapping("/{movieCode}/videos")
    public ResponseEntity<ApiSuccessResponse<List<VideoResponse>>> getTrailer(@PathVariable Integer movieCode) {
        return ResponseEntity.ok().body(ApiSuccessResponse.of("트레일러 조회 성공", movieService.getVideos(movieCode)));
    }
}
