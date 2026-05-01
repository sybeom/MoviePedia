package syb.moviepedia.movie.external.kmdb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import syb.moviepedia.movie.WeeklyBoxOfficeDto;


/**
 * KMDB api 불러올 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KmdbClient {
    private final WebClient kmdbWebClient;

    public WeeklyBoxOfficeDto fetchKmdbMovie(String movieName) {
        try {
            String json = fetchKmdbApiJson(movieName);
            return parseToWeeklyBoxOffice(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("KMDb 응답 파싱 실패", e); // TODO: 예외 클래스 만들기 및 글로벌 설정하기
        }
    }

    // KMDB API 호출
    private String fetchKmdbApiJson(String movieName) throws JsonProcessingException {
        return kmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/openapi-data2/wisenut/search_api/search_json2.jsp")
                        .queryParam("collection", "kmdb_new2")
                        .queryParam("ServiceKey", "EFJ0G903EF2P3C42QKU0")
                        .queryParam("listCount", 100)
                        .queryParam("startCount", 0)
                        .queryParam("title", movieName)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // json 파싱
    private WeeklyBoxOfficeDto parseToWeeklyBoxOffice(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode result = mapper.readTree(json).path("Data").get(0).path("Result").get(0);

        return WeeklyBoxOfficeDto.builder()
                .title(cleanTitle(result.path("title").asText()))
                .poster(result.path("posters").asText())
//                .releaseDate(result.path("repRlsDate").asText())
                .genre(result.path("genre").asText())
//                .rating(result.path("rating").asText())
//                .runtime(result.path("runtime").asText() + "분")
                .nation(result.path("nation").asText())
//                .plot(result.path("plots").path("plot").get(0).path("plotText").asText())
                .build();
    }

    private String cleanTitle(String title) {
        if (title == null) {
            return null;
        }

        return title
                .replace("!HS", "")
                .replace("!HE", "")
                .trim();
    }
}
