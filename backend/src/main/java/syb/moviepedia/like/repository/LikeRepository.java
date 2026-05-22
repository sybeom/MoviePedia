package syb.moviepedia.like.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import syb.moviepedia.comment.domain.Comment;
import syb.moviepedia.like.domain.Like;
import syb.moviepedia.member.domain.Member;

import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Integer> {
    Boolean existsByCommentId(Long commentId);

    Boolean existsByCommentIdAndMemberId(Long commentId, Long memberId);

    @Query("""
        select l.comment.id
        from Like l
        where l.member.id = :memberId
            and l.comment.id in :commentIds
    """)
    List<Long> findLikeIdsByMemberIdAndCommentIds(
            @Param("memberId") Long memberId,
            @Param("commentIds") List<Long> commentIds
    );
}
