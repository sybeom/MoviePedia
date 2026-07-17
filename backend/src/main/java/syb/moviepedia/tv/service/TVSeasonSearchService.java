package syb.moviepedia.tv.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.movie.dto.response.KeywordResponse;
import syb.moviepedia.tv.domain.TV;
import syb.moviepedia.tv.domain.TVSeasonDocument;
import syb.moviepedia.tv.dto.response.AllTVsResponse;
import syb.moviepedia.tv.dto.response.TVSeasonResponse;
import syb.moviepedia.tv.repsitory.TVRepository;
import syb.moviepedia.tv.repsitory.TVSeasonSearchRepository;

import java.util.List;

import static syb.moviepedia.tv.domain.QTV.tV;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class TVSeasonSearchService {

    private final TVSeasonSearchRepository tvSearchRepo;
    private final TVRepository tvRepo;
    private final JPAQueryFactory query;

    // 키워드 TV 제목 목록 조회
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

    // 키워드 TV 목록 조회
    public List<AllTVsResponse> getKeywordTVs(String keyword) {
        // 키워드에 대한 TV 목록 도큐먼트 조회
        List<TVSeasonDocument> tvDocs = tvSearchRepo.findByTitle(keyword);

        if (tvDocs.isEmpty()) {
            return List.of();
        }

        // 시즌 코드와 시즌 번호 별도 추출 후 저장
        List<TVSeriesAndSeason> ssList = tvDocs.stream().map(doc ->
                new TVSeriesAndSeason(Integer.parseInt(doc.getSeriesCode()), Integer.parseInt(doc.getSeasonNumber()))
        ).toList();

        BooleanBuilder condition = new BooleanBuilder();

        for (TVSeriesAndSeason ss : ssList) {
            condition.or(
                    allEq(ss.seriesCode(), ss.seasonNum())
            );
        }

        List<TV> tvs = query
                .select(tV)
                .from(tV)
                .where(condition)
                .fetch();

        return tvs.stream().map(tv ->
                AllTVsResponse.from(tv))
                .toList();
    }

    private BooleanExpression allEq(Integer seriesCode, Integer seasonNum) {
        if (seriesCode == null || seasonNum == null) {
            return null;
        }
        return tV.seriesCode.eq(seriesCode).and(tV.seasonNum.eq(seasonNum));
    }

    private record TVSeriesAndSeason(
        Integer seriesCode,
        Integer seasonNum) {
    }
}
