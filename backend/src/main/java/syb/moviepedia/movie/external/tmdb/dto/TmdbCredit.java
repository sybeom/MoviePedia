package syb.moviepedia.movie.external.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 출연진 api 응답 데이터 맵핑 클래스. 응답의 cast를 매핑한다
 */
public record TmdbCredit(
        List<TmdbCast> cast,
        List<TmdbCrew> crew
) {
    /**
     * credit api 응답의 cast 배열 매핑클래스.
     * 배열의 각 원소를 매핑.
     */
    public record TmdbCast(
            String name,
            @JsonProperty("original_name")
            String originalName,
            @JsonProperty("profile_path")
            String profile,
            @JsonProperty("order")
            Integer castOrder
    ) {}
    
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
}
