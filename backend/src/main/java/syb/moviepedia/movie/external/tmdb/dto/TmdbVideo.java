package syb.moviepedia.movie.external.tmdb.dto;

public record TmdbTrailer(
        String key,
        String type,
        boolean official
) {
}
