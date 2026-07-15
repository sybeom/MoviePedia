package syb.moviepedia.tv.repsitory;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import syb.moviepedia.tv.domain.TVSeasonDocument;

import java.util.List;

public interface TVSeasonSearchRepository extends ElasticsearchRepository<TVSeasonDocument, String> {
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
    List<TVSeasonDocument> findByTitle(String keyword);
}
