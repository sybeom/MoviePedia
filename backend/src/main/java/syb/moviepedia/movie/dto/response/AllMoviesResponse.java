package syb.moviepedia.movie.dto.response;

import lombok.Builder;
import syb.moviepedia.movie.domain.Movie;

@Builder
public record AllMoviesResponse(
        Integer code,
        String posterPath,
        String title,
        String certification
) {

    public static AllMoviesResponse from(Movie mv) {
        return AllMoviesResponse.builder()
                .code(mv.getCode())
                .posterPath(mv.getPosterPath())
                .title(mv.getTitle())
                .certification(mv.getCertification())
                .build();
    }
}
