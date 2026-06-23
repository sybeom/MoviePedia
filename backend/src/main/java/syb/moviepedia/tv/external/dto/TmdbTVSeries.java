package syb.moviepedia.tv.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Tmdb TV api(/tv/{series_id}) 호출 결과
 */
public record TmdbTVSeries(
        @JsonProperty("name")
        String title,

        @JsonProperty("genre_ids")
        List<Integer> genres,

        @JsonProperty("origin_country")
        List<String> countries,

        @JsonProperty("number_of_seasons")
        Integer numberOfSeasons
) {

}
