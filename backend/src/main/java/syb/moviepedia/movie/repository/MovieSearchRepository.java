package syb.moviepedia.movie.repository;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import syb.moviepedia.movie.domain.MovieDocument;

import java.util.List;

public interface MovieSearchRepository extends ElasticsearchRepository<MovieDocument, String> {

    // 검색 품질 테스트 2 match 쿼리 사용
    @Query("""
    {
      "multi_match": {
        "query": "?0",
        "fields": [
          "title",
          "title.space_ngram",
          "title.no_space_ngram",
          "keyword"
        ],
        "operator": "and"
      }
    }
    """)
    List<MovieDocument> findByTitle(String keyword);
}
