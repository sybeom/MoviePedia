package syb.moviepedia.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 코멘트 저장 요청 및 상세 응답 DTO
 */
// TODO: 작성날짜 추가
@Builder
public record CommentDto(
        @NotBlank String nickname,
        @NotNull String content,
        @NotNull Double rating,
        @NotNull Integer like
) {
}
