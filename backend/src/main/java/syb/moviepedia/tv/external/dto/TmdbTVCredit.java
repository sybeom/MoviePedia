package syb.moviepedia.tv.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbTVCredit(
        List<TmdbTVCast> cast,
        List<TmdbTVCrew> crew
) {

    public record TmdbTVCast(
            String name,
            @JsonProperty("original_name")
            String originalName,
            @JsonProperty("profile_path")
            String profile,
            @JsonProperty("order")
            Integer castOrder
    ) {
    }

    public record TmdbTVCrew(
            String name,
            @JsonProperty("profile_path")
            String profile,
            String job
    ) {

    }
}
