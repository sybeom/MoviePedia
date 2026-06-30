package syb.moviepedia.tv.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.json.Json;

import java.util.List;

/**
 * Tmdb TV discover api 호출
 * 해당 api는 TV 시리즈 목록들을 불러온다.
 */
public record TmdbTVDiscover(
        List<TmdbTVResult> results
) {
    public record TmdbTVResult(
            @JsonProperty("id")
            Integer code,

            @JsonProperty("name")
            String title,

            @JsonProperty("genre_ids")
            List<Integer> genreIds,

            @JsonProperty("origin_country")
            List<String> countries
    ) { }
}
