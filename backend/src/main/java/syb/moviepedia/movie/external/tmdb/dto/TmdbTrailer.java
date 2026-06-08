package syb.moviepedia.movie.external.tmdb.dto;

import lombok.ToString;

public record TmdbTrailer(
        String key,
        String type
) {
}
