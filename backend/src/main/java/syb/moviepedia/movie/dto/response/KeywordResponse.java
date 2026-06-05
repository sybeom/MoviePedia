package syb.moviepedia.movie.dto.response;

import lombok.Builder;
import lombok.ToString;


@Builder
public record KeywordResponse(
        String code,
        String title
) {
}
