package syb.moviepedia.movie.dto.response;

import lombok.Builder;

@Builder
public record GenreResponse(
        String name
) {
}
