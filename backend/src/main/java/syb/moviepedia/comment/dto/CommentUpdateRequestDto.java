package syb.moviepedia.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CommentUpdateRequestDto(
        @NotNull Long id,
        @NotNull String content,
        @NotNull Double rating,
        @NotNull Integer like
) {
}
