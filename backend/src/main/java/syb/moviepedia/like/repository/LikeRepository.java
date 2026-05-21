package syb.moviepedia.like.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import syb.moviepedia.like.domain.Like;

public interface LikeRepository extends JpaRepository<Like, Integer> {

}
