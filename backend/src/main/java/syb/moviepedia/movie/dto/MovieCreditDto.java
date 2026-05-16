package syb.moviepedia.movie.dto;

import lombok.Builder;
import syb.moviepedia.common.CreditRole;
import syb.moviepedia.movie.domain.Movie;

@Builder
public record MovieCreditDto(
        CreditRole role,
        String name,
        String profile
) {
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/original";

    public MovieCreditDto {
        profile = profile != null ? IMAGE_BASE_URL + profile : "";
    }
}
