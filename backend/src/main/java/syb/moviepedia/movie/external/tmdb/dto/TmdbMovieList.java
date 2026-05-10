package syb.moviepedia.movie.external.tmdb.dto;

import java.util.List;

/**
 * 카테고리(인기, 개봉 예정, 상영중), /discover api 호출 응답을 매핑하는 클래스
 * @param results
 */
public record TmdbMovieList(
        List<TmdbInitMovie> results
) {
}
