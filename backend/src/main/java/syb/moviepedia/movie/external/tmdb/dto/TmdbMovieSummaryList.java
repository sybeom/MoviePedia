package syb.moviepedia.movie.external.tmdb.dto;

import java.util.List;

/**
 * 홈 화면에 표시될 영화 요약 클래스 (인기 영화, 개봉예정, 상영중 영화 등)
 * api로 오는 json 데이터에서 필드에 맞게 자동 매핑된다.
 * 즉, 필드명과 키 명이 동일해야한다.
 */
public record TmdbMovieSummaryList(
        List<TmdbMovieSummary> results
) {
}
