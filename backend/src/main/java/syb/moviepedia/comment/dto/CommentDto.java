package syb.moviepedia.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 코멘트 요청 DTO
 */
// TODO: 닉네임, 및 작성날짜 추가
@Builder
public record CommentDto(
        @NotBlank String nickname,
        @NotNull String content,
        @NotNull Double rating
) {
}
