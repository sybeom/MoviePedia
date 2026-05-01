package syb.moviepedia.movie.external.kobis;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.StreamSupport;

import static java.time.DayOfWeek.SUNDAY;

/**
 * 영화진흥위원회 api를 불러올 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KobisClient {
    private final WebClient webClient;

    public List<KobisBoxOfficeInfo> fetchWeeklyBoxOffice() throws JsonProcessingException {
        String json = fetchWeeklyBoxOfficeJson();
        return parseToBoxOfficeInfo(json);
    }

    // 박스 오피스 Api 호출
    private String fetchWeeklyBoxOfficeJson() {
        String targetDt = LocalDateTime.now()
                .with(TemporalAdjusters.previous(SUNDAY))
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/boxoffice/searchWeeklyBoxOfficeList.json") // /openapi-data2/wisenut/search_api/search_json2.jsp
                        .queryParam("key", "27bf7372d6e67e530a9406bf1de74cfc")
                        .queryParam("targetDt", targetDt)
                        .queryParam("weekGb", "0")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // json 파싱
    private List<KobisBoxOfficeInfo> parseToBoxOfficeInfo(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json).path("boxOfficeResult").path("weeklyBoxOfficeList");

        return StreamSupport.stream(jsonNode.spliterator(), false)
                .map(movie ->
                        KobisBoxOfficeInfo.builder()
                                .rank(movie.path("rank").asInt())
                                .movieName(movie.path("movieNm").asText())
                                .build())
                .toList();
    }
}
