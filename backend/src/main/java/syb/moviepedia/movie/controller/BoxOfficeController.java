package syb.moviepedia.movie.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import syb.moviepedia.common.api.ApiResult;

/**
 * 박스 오피스 컨트롤러를 굳이 나눈이유는
 * /movies/weekly 같은 uri를 하면 주간 영화목록인지 주간 박스오피스인지 모호해지기때문이다.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/box-office")
public class BoxOfficeController {
    private final WebClient webClient;
    String apiUrl = "https://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json?key=27bf7372d6e67e530a9406bf1de74cfc&targetDt=20260428";

    @GetMapping("/weekly")
    public ResponseEntity<ApiResult<String>> getMovies() {
        String block = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.koreafilm.or.kr")
                        .path("/openapi-data2/wisenut/search_api/search_json2.jsp")
                        .queryParam("collection", "kmdb_new2")
                        .queryParam("ServiceKey", "EFJ0G903EF2P3C42QKU0")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return ResponseEntity.ok().body(ApiResult.success("주간 박스 오피스 조회 성공", block));
    }
}
