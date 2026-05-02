package syb.moviepedia.movie.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syb.moviepedia.common.api.ApiResult;
import syb.moviepedia.movie.dto.PopularMovieDto;
import syb.moviepedia.movie.service.MovieService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/movies")
public class MovieController {
    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<ApiResult<List<PopularMovieDto>>> getPopularMovies() {
        return ResponseEntity.ok().body(ApiResult.success("TMDB 인기 영화 목록", movieService.getPopularMovies()));
    }
}
