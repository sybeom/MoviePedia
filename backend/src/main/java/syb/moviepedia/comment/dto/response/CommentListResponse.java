package syb.moviepedia.comment.dto.response;

import lombok.Builder;

import java.util.List;

/**
 * 코멘트 목록 응답 DTO
 */
@Builder
public record CommentListResponse(
        Long id, // 코멘트 수정, 삭제시 필요
        List<CommentResponse> comments
) {
}
