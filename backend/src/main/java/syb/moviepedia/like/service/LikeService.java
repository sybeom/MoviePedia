package syb.moviepedia.like.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import syb.moviepedia.comment.domain.Comment;
import syb.moviepedia.comment.repository.CommentRepository;
import syb.moviepedia.common.exception.CommentNotFoundException;
import syb.moviepedia.like.domain.Like;
import syb.moviepedia.like.repository.LikeRepository;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    public void saveLike(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("코멘트를 찾을 수 없습니다, id: " + commentId));

        Like like = Like.builder()
                .comment(comment)
                .member(comment.getMember())
                .build();

        likeRepository.save(like);
    }
}
