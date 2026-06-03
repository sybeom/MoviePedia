package syb.moviepedia.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import syb.moviepedia.elasticsearch.index.UserDocument;

public interface UserDocumentRepository extends ElasticsearchRepository<UserDocument, String> {

}
