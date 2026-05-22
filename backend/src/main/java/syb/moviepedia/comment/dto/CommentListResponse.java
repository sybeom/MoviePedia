package syb.moviepedia.comment.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record CommentListResponse(
        Long movieId,
        List<CommentResponseDto> comments
) {
}
