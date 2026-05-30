package syb.moviepedia.comment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 코멘트 업데이트시 요청 DTO
 */
@Builder
public record CommentUpdateRequest (
        @NotNull Long movieId,
        @NotNull String content
) {
}
