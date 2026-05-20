package syb.moviepedia.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 코멘트 업데이트 할 때 필요한 DTO
 */
@Builder
public record CommentUpdateRequestDto(
        @NotNull Long id,
        @NotNull String content,
        @NotNull Double rating,
        @NotNull Integer like
) {
}
