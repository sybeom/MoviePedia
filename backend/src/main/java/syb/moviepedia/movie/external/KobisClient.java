package syb.moviepedia.movie.external;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

import static java.time.DayOfWeek.*;
import static java.time.LocalTime.now;

/**
 * 영화진흥위원회 api를 불러올 클래스
 */
@Component
@RequiredArgsConstructor
public class KobisClient {
    private final WebClient webClient;

    public String getWeeklyBoxOffice() {
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
}
