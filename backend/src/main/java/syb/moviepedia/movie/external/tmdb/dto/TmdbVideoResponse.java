package syb.moviepedia.movie.external.tmdb.dto;

import java.util.List;

public record TmdbVideoResponse(
        List<TmdbVideo> results
) {
}
