package syb.moviepedia.movie.external.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 검색어 api 응답데이터 results 필드의 각 원소를 나타낸다
 * 검색어 목록만 보여줄 예정이므로 title만 가져온다.
 */
public record TmdbKeyword(
        Long id,
        String title
) {
}
