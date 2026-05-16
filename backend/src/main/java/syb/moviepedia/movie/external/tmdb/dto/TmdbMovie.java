package syb.moviepedia.movie.external.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

/**
 * /discover api 호출 응답의 results를 매핑하는 클래스
 */
public record TmdbMovie(
        @JsonProperty("id")
        Long code,

        String title,

        @JsonProperty("backdrop_path")
        String backdropPath,

        @JsonProperty("poster_path")
        String posterPath,

        @JsonProperty("genre_ids")
        List<Integer> genres,

        String overview,

        @JsonProperty("release_date")
        LocalDate releaseDate,

        @JsonProperty("origin_country")
        List<String> country,

        Integer runtime,

        Double popularity,

        @JsonProperty("vote_average")
        Double globalRating
) {
        public TmdbMovie {
                // 0점이면 null로 표기 (아직 평점 책정이 되지 않았다는 뜻이다)
                globalRating = globalRating==0 ? null : Math.round(globalRating * 10) / 10.0; // 소수점 둘째자리에서 반올림
        }
}
