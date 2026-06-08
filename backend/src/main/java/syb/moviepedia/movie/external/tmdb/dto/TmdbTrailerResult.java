package syb.moviepedia.movie.external.tmdb.dto;

import java.util.List;

public record TmdbTrailerResult(
        List<TmdbTrailer> results
) {
}
