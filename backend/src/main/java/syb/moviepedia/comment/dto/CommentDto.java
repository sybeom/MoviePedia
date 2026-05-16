package syb.moviepedia.comment.dto;

/**
 * 코멘트 요청 DTO
 */
public record CommentDto(
        String content,
        Double rating
) {
}
