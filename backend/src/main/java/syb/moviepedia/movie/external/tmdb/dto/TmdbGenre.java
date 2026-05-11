package syb.moviepedia.movie.external.tmdb.dto;

/**
 * /genre/movie/list TMDB api의 응답 데이터를 매핑할 클래스
 * 영화 api로 오는 json 데이터에서 필드에 맞게 자동 매핑된다.
 * 즉, 필드명과 키 명이 동일해야한다.
 */
public record TmdbGenre(
        Integer id,
        String name
) {
}