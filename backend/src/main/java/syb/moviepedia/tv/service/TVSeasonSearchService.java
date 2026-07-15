package syb.moviepedia.tv.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.movie.domain.MovieDocument;
import syb.moviepedia.movie.dto.response.KeywordResponse;
import syb.moviepedia.tv.domain.TVSeasonDocument;
import syb.moviepedia.tv.repsitory.TVSeasonSearchRepository;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class TVSeasonSearchService {

    private final TVSeasonSearchRepository tvSearchRepo;

    public List<KeywordResponse> getKeywords(String keyword) {
        log.info("요청 키워드 : {}", keyword);
        List<TVSeasonDocument> results = tvSearchRepo.findByTitle(keyword);
        log.info("키워드 목록 호출 결과 : {}", results);

        return toKeywordsResponse(results);
    }

    private List<KeywordResponse> toKeywordsResponse(List<TVSeasonDocument> results) {
        return results.stream().map(doc ->
                        KeywordResponse.builder()
                                .code(doc.getSeriesCode())
                                .seasonNum(doc.getSeasonNumber())
                                .title(doc.getDisplayTitle())
                                .build())
                .toList();
    }
}
