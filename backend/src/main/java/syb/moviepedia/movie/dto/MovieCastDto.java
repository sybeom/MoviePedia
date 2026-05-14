package syb.moviepedia.movie.dto;

import lombok.Builder;
import syb.moviepedia.movie.domain.Movie;

@Builder
public record MovieCastDto(
        Movie movie,
        Long actorId,
        String name,
        String profile,
        Integer order
) {
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/original";

    public MovieCastDto {
        profile = profile() != null ? IMAGE_BASE_URL + profile() : "";
    }
}
