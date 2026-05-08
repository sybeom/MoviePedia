package syb.moviepedia.movie.external.tmdb.dto;

import java.util.List;

/**
 * 장르 번호와 장르명이 담겨있는 리스트를 담는 클래스
 * 영화 api로 오는 json 데이터에서 필드에 맞게 자동 매핑된다.
 * 즉, 필드명과 키 명이 동일해야한다.
 */
public record TmdbGenreList(
        List<TmdbGenreResponse> genres
) {
}
