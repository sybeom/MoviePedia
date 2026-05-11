package syb.moviepedia.movie.external.tmdb.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * /configuration/countries tmdb api의 응답 데이터를 매핑할 클래스
 */
public record TmdbCountry(
        @JsonProperty("iso_3166_1")
        String code,
        @JsonProperty("native_name")
        String name
) {
}
