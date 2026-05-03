package syb.moviepedia.movie.external.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * TMDB Api 메인 화면에 표시될 카테고리 별 영화 클래스
 * json 응답에서 필드에 맞게 자동 매핑된다.
 * 즉, 필드명과 json의 키 명이 동일해야한다.
 */
public record TmdbMovieSummary(
        Long id,
        String title,
        @JsonProperty("poster_path")
        String posterPath,
        @JsonProperty("genre_ids")
        List<Integer> genreIds,
        @JsonProperty("vote_average")
        String voteAverage
) {}
