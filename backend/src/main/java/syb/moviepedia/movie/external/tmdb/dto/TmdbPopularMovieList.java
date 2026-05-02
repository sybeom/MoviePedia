package syb.moviepedia.movie.external.tmdb.dto;

import java.util.List;

/**
 * 인기 영화 목록 클래스
 * 인기 영화 api로 오는 json 데이터에서 필드에 맞게 자동 매핑된다.
 * 즉, 필드명과 키 명이 동일해야한다.
 * 영화 정렬 기준은 popularity.desc이다.
 */
public record TmdbPopularMovieList(
        List<TmdbPopularMovie> results
) {
}
