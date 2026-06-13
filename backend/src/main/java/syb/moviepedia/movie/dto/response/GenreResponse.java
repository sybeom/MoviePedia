package syb.moviepedia.movie.dto.response;

import lombok.Builder;

@Builder
public record GenreResponse(
        Integer genreId,
        String name
) {
}
