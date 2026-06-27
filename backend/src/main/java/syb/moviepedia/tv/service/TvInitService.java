package syb.moviepedia.tv.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.movie.repository.CountryRepository;
import syb.moviepedia.tv.domain.TV;
import syb.moviepedia.tv.external.TmdbTVClient;
import syb.moviepedia.tv.external.dto.TmdbTVDiscover;
import syb.moviepedia.tv.repsitory.TVRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TvInitService {
    private final TVRepository tvRepository;
    private final TmdbTVClient tmdbTVClient;
    private final CountryRepository countryRepo;
    private static final String TITLE_PATTERN = "^(?!(?=.*\\p{L})(?!.*[가-힣]))[\\p{L}0-9 .,:~!?'\"/(){}\\[\\]&+\\-·]+$";

    public void initTV(int page) {
        // 시리즈 id를 반환
        TmdbTVDiscover tvSeries = tmdbTVClient.fetchTVSeries(page);

        List<TV> tvs = tvSeries.results().stream()
                .map(result -> tmdbTVClient.fetchTVSeriesDetail(result.id()))
                .filter(series -> series.title().matches(TITLE_PATTERN))
                .flatMap(series -> series.seasons().stream()
                        .filter(season -> season.seasonNumber() != 0)
                        .map(season -> TV.builder()
                                .code(series.code())
                                .title(series.title())
                                .seasonNum(season.seasonNumber())
                                .episodeCnt(season.episodeCnt())
                                .posterPath(season.posterPath())
                                .overview(season.overview())
                                .country(countryRepo.findNameByCodeIn(series.countries()))
                                .detailFetched(false)
                                .build()
                        )
                )
                .toList();

        tvRepository.saveAll(tvs);

    }
}
