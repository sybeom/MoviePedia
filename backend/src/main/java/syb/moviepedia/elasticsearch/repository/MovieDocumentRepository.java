package syb.moviepedia.elasticsearch.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import syb.moviepedia.elasticsearch.domain.MovieDocument;

public interface MovieDocumentRepository extends ElasticsearchRepository<MovieDocument, String> {

}
