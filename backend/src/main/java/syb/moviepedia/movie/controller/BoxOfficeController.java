package syb.moviepedia.movie.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syb.moviepedia.common.api.ApiResult;
import syb.moviepedia.movie.external.kobis.KobisBoxOfficeInfo;
import syb.moviepedia.movie.service.BoxOfficeService;

import java.util.List;

/**
 * 박스 오피스 컨트롤러를 굳이 나눈이유는
 * /movies/weekly 같은 uri를 하면 주간 영화목록인지 주간 박스오피스인지 모호해지기때문이다.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/box-office")
public class BoxOfficeController {

    private final BoxOfficeService boxOfficeService;

    @GetMapping
    public ResponseEntity<ApiResult<String>> getMovies() throws JsonProcessingException {
        List<KobisBoxOfficeInfo> boxOfficeWeekly = boxOfficeService.getBoxOfficeWeekly();
        return ResponseEntity.ok().body(ApiResult.success("주간 박스 오피스 조회 성공", "ASd"));
    }
}
