package syb.moviepedia.comment.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 코멘트 요청 DTO
 */
public record CommentDto(
        @NotNull String content,
        @NotNull Double rating
) {
}
