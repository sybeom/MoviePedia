package syb.moviepedia.tv.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbTVCategory(
        List<TmdbTVCategoryResult> results
) {

    public record TmdbTVCategoryResult(
            @JsonProperty("id")
            Integer code,

            @JsonProperty("name")
            String title,

            @JsonProperty("backdrop_path")
            String backdropPath
    ) {

    }
}
