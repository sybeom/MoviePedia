package syb.moviepedia.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import syb.moviepedia.comment.domain.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
