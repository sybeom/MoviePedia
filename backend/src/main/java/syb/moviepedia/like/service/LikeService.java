package syb.moviepedia.like.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import syb.moviepedia.comment.domain.Comment;
import syb.moviepedia.comment.repository.CommentRepository;
import syb.moviepedia.common.exception.AlreadyLikedException;
import syb.moviepedia.common.exception.CannotLikeOwnCommentException;
import syb.moviepedia.common.exception.CommentNotFoundException;
import syb.moviepedia.common.exception.MemberNotFoundException;
import syb.moviepedia.like.domain.Like;
import syb.moviepedia.like.repository.LikeRepository;
import syb.moviepedia.member.domain.Member;
import syb.moviepedia.member.repository.MemberRepository;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void saveLike(Long commentId, String loginId) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new MemberNotFoundException("해당 멤버를 찾을 수 없습니다. loginId: " + loginId));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("코멘트를 찾을 수 없습니다, id: " + commentId));


        if (Objects.equals(comment.getMember().getLoginId(), loginId)) {
            throw new CannotLikeOwnCommentException("자신의 코멘트에는 좋아요를 누를 수 없습니다.");
        }

        Boolean exist = likeRepository.existsByCommentIdAndMemberId(comment.getId(), member.getId());

        if (exist) { // 이미 좋아요를 누른 경우
            throw new AlreadyLikedException("이미 해당 코멘트에 좋아요를 눌렀습니다.");
        }

        Like like = Like.builder()
                .comment(comment)
                .member(member)
                .build();

        likeRepository.save(like);
        comment.increaseLike();
    }
}
