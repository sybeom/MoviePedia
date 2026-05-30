package syb.moviepedia.comment.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 코멘트 수정 응답 Dto
 */
@Builder
public record CommentEditResponse(
        @NotNull String content
) {
}
