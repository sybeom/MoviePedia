package syb.moviepedia.movie.external.tmdb.dto;

import java.util.List;

/**
 * 검색어 api 응답 데이터 매핑 클래스
 */
public record TmdbKeywordList(
        List<TmdbKeyword> results
) {
}
