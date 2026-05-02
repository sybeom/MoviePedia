package syb.moviepedia.movie.external.tmdb.dto;

/**
 * TMDB 장르 API의 응답 데이터를 받을 클래스
 * 인기 영화 api로 오는 json 데이터에서 필드에 맞게 자동 매핑된다.
 * 즉, 필드명과 키 명이 동일해야한다.
 */
public record TmdbGenreResponse(
        Integer id,
        String name
) {
}