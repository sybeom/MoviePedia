package syb.moviepedia.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 수정 코멘트 응답 Dto
 */
@Builder
public record EditCommentResponseDto(
        @NotNull String content,
        @NotNull Double rating
) {
}
