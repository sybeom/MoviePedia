package syb.moviepedia.tv.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.common.MediaCategoryType;
import syb.moviepedia.movie.repository.CountryRepository;
import syb.moviepedia.tv.domain.TV;
import syb.moviepedia.tv.domain.TVCategory;
import syb.moviepedia.tv.external.TmdbTVClient;
import syb.moviepedia.tv.external.dto.TmdbTVDiscover;
import syb.moviepedia.tv.repsitory.TVCategoryRepository;
import syb.moviepedia.tv.repsitory.TVRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TVInitService {
    private final TVRepository tvRepo;
    private final TVCategoryRepository tvCategoryRepo;
    private final TmdbTVClient tmdbTVClient;
    private final CountryRepository countryRepo;
    private static final String TITLE_PATTERN = "^(?!(?=.*\\p{L})(?!.*[가-힣]))[\\p{L}0-9 .,:~!?'\"/(){}\\[\\]&+\\-·]+$";

    public void initCategories() {
//        Set<Integer> seriesCodes = tmdbTVClient.fetchTVPopularCategories().results().stream()
//                .filter(result -> isTitleMatch(result.title()))
//                .map(result -> result.code())
//                .collect(Collectors.toSet());

//        List<TV> tvList = tvRepo.findByPopularSeason(seriesCodes);

//        List<TVCategory> categories = tvList.stream().map(tv -> TVCategory.builder()
//                        .code(tv.getCode())
//                        .title(tv.getTitle())
//                        .backdropPath("E")
//                        .mediaCategoryType(MediaCategoryType.POPULAR)
//                        .tv(tv)
//                        .build())
//                .toList();
        List<TVCategory> categories = tmdbTVClient.fetchTVPopularCategories().results().stream()
                .filter(result -> isTitleMatch(result.title()))
                .map(result -> TVCategory.builder()
                        .code(result.code())
                        .title(result.title())
                        .backdropPath(result.backdropPath())
                        .mediaCategoryType(MediaCategoryType.POPULAR)
                        .build())
                .toList();

        tvCategoryRepo.saveAll(categories);
    }

    public void initTV(int page) {
        // 시리즈 id를 반환
        TmdbTVDiscover tvSeries = tmdbTVClient.fetchTVSeries(page);

        List<TV> tvs = tvSeries.results().stream()
                .map(result -> tmdbTVClient.fetchTVSeriesDetail(result.id()))
                .filter(series -> isTitleMatch(series.title()))
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

        tvRepo.saveAll(tvs);

    }

    private boolean isTitleMatch(String title) {
        return title.matches(TITLE_PATTERN);
    }
}
