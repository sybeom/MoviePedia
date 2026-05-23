package syb.moviepedia.comment.dto.response;

import lombok.Builder;

import java.util.List;

/**
 * 코멘트 목록 응답 DTO
 */
@Builder
public record CommentListResponse(
        Long movieId,
        List<CommentResponse> comments
) {
}
