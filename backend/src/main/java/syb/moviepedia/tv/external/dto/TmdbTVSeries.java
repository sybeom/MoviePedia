package syb.moviepedia.tv.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Tmdb TV api(/tv/{series_id}) 호출 결과
 */
public record TmdbTVSeries(
        @JsonProperty("id")
        Integer code,

        @JsonProperty("name")
        String title, // 시리즈 제목

        @JsonProperty("genre_ids")
        List<Integer> genres,

        @JsonProperty("origin_country")
        List<String> countries,

        List<TVSeasons> seasons
) {
        public record TVSeasons(
                @JsonProperty("id")
                Integer seasonCode,

                @JsonProperty("name")
                String title,

                String overview,

                @JsonProperty("poster_path")
                String posterPath,

                @JsonProperty("season_number")
                Integer seasonNumber,

                @JsonProperty("episode_count")
                Integer episodeCnt
        ) {

        }
}
