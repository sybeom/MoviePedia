package syb.moviepedia.comment.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 상세 응답 DTO
 */
// TODO: 작성날짜 추가
@Builder
public record CommentDetailResponse(
        @NotBlank String nickname,
        @NotNull
        String content
) {
}
