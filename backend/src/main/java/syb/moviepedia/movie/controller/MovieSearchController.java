package syb.moviepedia.movie.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import syb.moviepedia.common.api.ApiSuccessResponse;
import syb.moviepedia.common.swagger.SwaggerApiResponse;
import syb.moviepedia.movie.dto.response.AllMoviesResponse;
import syb.moviepedia.movie.dto.response.KeywordResponse;
import syb.moviepedia.movie.service.MovieSearchService;

import java.util.List;

@Tag(name = "Movie Search API", description = "영화 검색 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/movies/search")
public class MovieSearchController {

    private final MovieSearchService movieSearchService;

    @Operation(summary = "영화 검색어 목록", description = "키워드에 대한 관련 영화 검색어 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색어 목록 조회 성공"),
            @ApiResponse(
                    responseCode = "502", description = "검색어 조회 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            )

    })
    @GetMapping("/titles")
    public ResponseEntity<ApiSuccessResponse<List<KeywordResponse>>> getMovieKeywords(@RequestParam String keyword) {
        return ResponseEntity.ok().body(ApiSuccessResponse.of("영화 검색 목록", movieSearchService.getKeywords(keyword)));
    }

    @Operation(summary = "키워드 영화 결과 목록", description = "키워드에 대한 영화 검색 결과 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "키워드 영화 검색 결과 조회 성공"),
            @ApiResponse(
                    responseCode = "502", description = "키워드 영화 검색 결과 조회 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponse.class)
                    )
            )

    })
    @GetMapping
    public ResponseEntity<ApiSuccessResponse<List<AllMoviesResponse>>> getSearchedMovie(@RequestParam String keyword) {
        return ResponseEntity.ok().body(ApiSuccessResponse.of("키워드 영화 검색 결과 목록", movieSearchService.getKeywordMovies(keyword)));
    }
}
