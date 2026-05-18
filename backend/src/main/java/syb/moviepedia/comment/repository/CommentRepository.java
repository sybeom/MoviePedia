package syb.moviepedia.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import syb.moviepedia.comment.domain.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByMovieId(Long movieId);
    Boolean existsByMovieIdAndMemberId(Long  movieId, Long memberId);

    @Query("""
        select c
        from Comment c
        where c.movie.id = :movieId
        order by 
            case when c.member.loginId = :loginId then 0 else 1 end
    """)
    List<Comment> findByMovieIdWithMyCommentFirst(
            @Param("movieId") Long movieId,
            @Param("loginId") String loginId);
}
