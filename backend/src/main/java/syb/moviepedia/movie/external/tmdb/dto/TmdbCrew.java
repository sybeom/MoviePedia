package syb.moviepedia.movie.external.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * credit api 응답의 crew 배열 매핑 클래스.
 * 배열의 각 원소를 매핑.
 * 조직 구성원을 나타낸다 (감독, 에디터, 아트 디렉터, 스턴트맨 등)
 */
public record TmdbCrew(
        String name,
        @JsonProperty("profile_path")
        String profile,
        String job
) {}
