package syb.moviepedia.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import syb.moviepedia.comment.domain.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByMovieId(Long movieId);
    Boolean existsByMovieIdAndMemberId(Long  movieId, Long memberId);
}
