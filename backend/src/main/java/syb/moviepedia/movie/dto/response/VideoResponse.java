package syb.moviepedia.movie.dto.response;

import lombok.Builder;
import syb.moviepedia.common.VideoType;

@Builder
public record VideoResponse(
        String key,
        VideoType type
) {
}
