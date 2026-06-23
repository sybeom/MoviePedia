package syb.moviepedia.tv.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbTVSeason(
        @JsonProperty("id")
        Integer seasonCode,

        @JsonProperty("name")
        String title,

        String overview,

        @JsonProperty("poster_path")
        String posterPath,

        @JsonProperty("season_number")
        Integer seasonNumber
) {
}
