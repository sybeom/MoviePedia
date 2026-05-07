package syb.moviepedia.movie.external.tmdb.dto;

import java.util.List;

/**
 * /discover api 호출 응답을 매핑하는 클래스
 * @param results
 */
public record TmdbInitMovieList(
        List<TmdbInitMovie> results
) {
}
