package syb.moviepedia.like.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import syb.moviepedia.comment.domain.Comment;
import syb.moviepedia.like.domain.Like;
import syb.moviepedia.member.domain.Member;

public interface LikeRepository extends JpaRepository<Like, Integer> {
    Boolean existsByCommentId(Long commentId);

    Boolean existsByCommentAndMember(Comment comment, Member member);
}
