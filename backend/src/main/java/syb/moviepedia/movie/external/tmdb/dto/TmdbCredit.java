package syb.moviepedia.movie.external.tmdb.dto;

import java.util.List;

/**
 * 출연진 api 응답 데이터 맵핑 클래스. 응답의 cast를 매핑한다
 */
public record TmdbCredit(
        List<TmdbCast> cast,
        List<TmdbCrew> crew
) {}
