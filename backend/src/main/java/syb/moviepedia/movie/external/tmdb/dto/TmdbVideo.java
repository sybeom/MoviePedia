package syb.moviepedia.movie.external.tmdb.dto;

import syb.moviepedia.common.VideoType;

public record TmdbVideo(
        String key,
        String site,
        VideoType type,
        boolean official
) {
}
