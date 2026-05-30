package syb.moviepedia.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import syb.moviepedia.common.ReactionType;

/**
 * 코멘트 등록 요청 DTO
 */
public record CommentSaveRequest(
        @NotBlank String nickname,
        @Size(min = 1, max = 300)
        @NotNull
        String content,
        ReactionType reactionType
) {
}
