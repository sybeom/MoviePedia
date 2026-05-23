package syb.moviepedia.comment.dto.response;

import lombok.Builder;

/**
 * 상세 페이지 코멘트 목록의 각 코멘트 개체 응답 DTO
 */
@Builder
public record CommentResponse(
        Long commentId,
        String nickname,
        String content,
        Double rating,
        Integer likeCount,
        Boolean likedByMe, // 나(현재 로그인 유저)에 의해 눌러짐
        Boolean writtenByMe // 나(현재 로그인 유저)에 의해 쓰여짐
) {
}
