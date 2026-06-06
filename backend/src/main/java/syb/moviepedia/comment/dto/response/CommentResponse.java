package syb.moviepedia.comment.dto.response;

import lombok.Builder;
import syb.moviepedia.common.ReactionType;

import java.time.LocalDate;

/**
 * 상세 페이지 코멘트 목록의 각 코멘트 개체 응답 DTO
 */
@Builder
public record CommentResponse(
        Long commentId, // 코멘트 수정, 삭제시 필요
        String nickname,
        String content,
        ReactionType reactionType,
        Boolean writtenByMe, // 나(현재 로그인 유저)에 의해 쓰여짐
        LocalDate createdAt
) {
}
