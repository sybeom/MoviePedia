package syb.moviepedia.movie.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import syb.moviepedia.movie.domain.MovieDocument;

import java.util.List;

public interface MovieSearchRepository extends ElasticsearchRepository<MovieDocument, String> {
    List<MovieDocument> findByTitle(String keyword);
}
