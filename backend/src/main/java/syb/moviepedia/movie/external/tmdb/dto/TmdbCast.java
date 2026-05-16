package syb.moviepedia.movie.external.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 출연진 api 응답 데이터 맵핑 클래스.
 * 응답의 cast 배열의 각 원소를 매핑.
 */
public record TmdbCast(
        @JsonProperty("id")
        Long actorId,
        String name,
        @JsonProperty("original_name")
        String originalName,
        @JsonProperty("profile_path")
        String profile,
        @JsonProperty("order")
        Integer castOrder
) {}
