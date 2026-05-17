package syb.moviepedia.comment.dto;

import lombok.Builder;

/**
 * 코멘트 목록 응답 DTO
 */
@Builder
public record CommentResponseDto(
        String nickname,
        String content,
        double rating
) {
}
