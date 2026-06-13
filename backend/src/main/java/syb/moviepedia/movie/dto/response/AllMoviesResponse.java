package syb.moviepedia.movie.dto.response;

import lombok.Builder;

@Builder
public record AllMoviesResponse(
        Long code,
        String posterPath,
        String title,
        String certification
) {
}
