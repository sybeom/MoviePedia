package syb.moviepedia;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import syb.moviepedia.movie.repository.CountryRepository;
import syb.moviepedia.tv.domain.TV;
import syb.moviepedia.tv.external.TmdbTVClient;
import syb.moviepedia.tv.external.dto.TmdbTVDiscover;
import syb.moviepedia.tv.repsitory.TVRepository;

import java.util.List;

@Slf4j
@SpringBootTest
public class TmdbApiTest {
    @Autowired
    TmdbTVClient tmdbTVClient;

    @Autowired
    TVRepository tvRepository;

    @Autowired
    CountryRepository countryRepo;

    private static final String TITLE_PATTERN = "^(?!(?=.*\\p{L})(?!.*[가-힣]))[\\p{L}0-9 .,:~!?'\"/(){}\\[\\]&+\\-·]+$";

    @Test
    public void tmdbApiTest(){

        // 시리즈 id를 반환
        TmdbTVDiscover discover = tmdbTVClient.getTvTest(1);
        org.assertj.core.api.Assertions.assertThat(discover).isNotNull();

        List<TV> tvs = discover.results().stream()
                .map(result -> tmdbTVClient.getTVSeries(result.id()))
                .filter(series -> series.title().matches(TITLE_PATTERN))
                .flatMap(series -> series.seasons().stream()
                        .map(season -> TV.builder()
                                .code(series.code())
                                .title(series.title())
                                .posterPath(season.posterPath())
                                .overview(season.overview())
                                .country(countryRepo.findNameByCodeIn(series.countries()))
                                .detailFetched(false)
                                .seasonNum(season.seasonNumber())
                                .build()
                        )
                )
                .toList();

        tvRepository.saveAll(tvs);
        log.info("값확인 {}", discover.toString());
    }
}
