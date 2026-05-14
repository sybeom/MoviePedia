package syb.moviepedia.movie.external.tmdb.dto;

import java.util.List;

public record TmdbCastList(
        List<TmdbCast> cast
) {}
