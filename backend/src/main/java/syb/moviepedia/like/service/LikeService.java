package syb.moviepedia.like.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.comment.domain.Comment;
import syb.moviepedia.comment.repository.CommentRepository;
import syb.moviepedia.common.exception.*;
import syb.moviepedia.like.domain.Like;
import syb.moviepedia.like.repository.LikeRepository;
import syb.moviepedia.member.domain.Member;
import syb.moviepedia.member.repository.MemberRepository;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void saveLike(Long commentId, String loginId) {
        Member member = findCurrentMember(loginId);

        Comment comment = getCurrentComment(commentId);


        if (Objects.equals(comment.getMember().getLoginId(), loginId)) {
            throw new CannotLikeOwnCommentException("자신의 코멘트에는 좋아요를 누를 수 없습니다.");
        }

        // 현재 로그인 유저의 id와 코멘트 id가 있는지 확인. 있다면 이미 좋아요 누른 상태
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

    @Transactional
    public void deleteLike(Long commentId, String loginId) {
        Member member = findCurrentMember(loginId);

        Comment comment = getCurrentComment(commentId);

        Boolean exist = likeRepository.existsByCommentIdAndMemberId(comment.getId(), member.getId());

        if (!exist) {
            throw new LikeNotFoundException("삭제할 좋아요를 찾을 수 없습니다.");
        }

        likeRepository.deleteByCommentIdAndMemberId(comment.getId(), member.getId());
        comment.decreaseLike();
    }

    // 현재 로그인 중인 멤버 엔티티 찾기
    private Member findCurrentMember(String loginId) {
        return memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new MemberNotFoundException("해당 멤버를 찾을 수 없습니다. loginId: " + loginId));
    }

    // 좋아요 대상이 되는 현재 코멘트
    private Comment getCurrentComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("코멘트를 찾을 수 없습니다, id: " + commentId));
    }
}
