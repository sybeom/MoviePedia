package syb.moviepedia.tv.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbContentRating(
        List<TmdbContentRatingInfo> results
) {
    public record TmdbContentRatingInfo(
            @JsonProperty("iso_3166_1")
            String countryCode,
            String rating
    ) {}
}
