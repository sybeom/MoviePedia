package syb.moviepedia.movie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import syb.moviepedia.movie.external.tmdb.dto.TmdbGenreList;

import java.util.List;

@Builder
public record MovieDetailDto(
        Long movieId,

        @JsonProperty("original_title")
        String title,

        @JsonProperty("backdrop_path")
        String backdropPath, // 상단 배너(배경)

        @JsonProperty("poster_path")
        String posterPath,

        List<TmdbGenreList> genres,

        String overview,

        @JsonProperty("release_date")
        String releaseDate,

        @JsonProperty("origin_country")
        String country,

        String runtime,

        @JsonProperty("vote_average")
        String globalRating // 글로벌 평점
) {
}
