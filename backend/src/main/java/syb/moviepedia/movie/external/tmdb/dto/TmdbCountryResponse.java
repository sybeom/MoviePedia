package syb.moviepedia.movie.external.tmdb.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbCountryResponse(
        @JsonProperty("iso_3166_1")
        String code,
        @JsonProperty("native_name")
        String name
) {
}
