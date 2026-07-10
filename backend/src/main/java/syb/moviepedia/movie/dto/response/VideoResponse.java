package syb.moviepedia.movie.dto.response;

import lombok.Builder;
import syb.moviepedia.common.VideoType;
import syb.moviepedia.movie.domain.Video;

@Builder
public record VideoResponse(
        String key,
        VideoType type
) {
    public static VideoResponse from(Video v) {
        return VideoResponse.builder()
                .key(v.getKey())
                .type(v.getVideoType())
                .build();
    }
}
