package syb.moviepedia.comment.dto;

import lombok.Builder;

/**
 * 코멘트 목록 응답 DTO
 */
@Builder
public record CommentResponseDto(
        Long commentId,
        Long movieId,
        String nickname,
        String content,
        Double rating,
        Integer likeCount,
        Boolean likedByMe, // 나(현재 로그인 유저)에 의해 눌러짐
        Boolean writtenByMe // 나(현재 로그인 유저)에 의해 쓰여짐
) {
}
