package syb.moviepedia.movie.external.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import syb.moviepedia.common.VideoType;

import java.time.Instant;

public record TmdbVideo(
        String key,
        String site,
        VideoType type,
        boolean official,
        @JsonProperty("published_at")
        Instant publishedAt
) {
}
