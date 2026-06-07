package syb.moviepedia.movie.repository;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import syb.moviepedia.movie.domain.MovieDocument;

import java.util.List;

public interface MovieSearchRepository extends ElasticsearchRepository<MovieDocument, String> {
    /**
     * TODO: DataInitializer에서 엘라스틱서치 데이터 삽입 필수
     */
    // 검색 품질 테스트 1
    List<MovieDocument> findByTitle(String keyword);

    // 검색 품질 테스트 2 match 쿼리 사용
//    @Query("{\"match\": {\"title\": \"?0\"}}")
//    List<MovieDocument> findByTitle(String keyword);
}
