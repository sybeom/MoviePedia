package syb.moviepedia.tv.service;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.common.CreditRole;
import syb.moviepedia.common.MediaType;
import syb.moviepedia.common.ReleaseStatus;
import syb.moviepedia.common.SortType;
import syb.moviepedia.common.exception.TVSeasonNotFoundException;
import syb.moviepedia.movie.domain.Credit;
import syb.moviepedia.movie.dto.request.FilterRequest;
import syb.moviepedia.movie.dto.response.GenreResponse;
import syb.moviepedia.movie.dto.response.MovieCreditResponse;
import syb.moviepedia.movie.external.tmdb.TmdbClient;
import syb.moviepedia.movie.external.tmdb.dto.TmdbCredit;
import syb.moviepedia.movie.repository.CreditRepository;
import syb.moviepedia.movie.repository.GenreRepository;
import syb.moviepedia.tv.domain.QTVSeries;
import syb.moviepedia.tv.domain.QTVSeriesGenre;
import syb.moviepedia.tv.domain.TV;
import syb.moviepedia.tv.domain.TVSeries;
import syb.moviepedia.tv.dto.response.AllTVsResponse;
import syb.moviepedia.tv.dto.response.TVPopularResponse;
import syb.moviepedia.tv.dto.response.TVSeasonCreditResponse;
import syb.moviepedia.tv.dto.response.TVSeasonResponse;
import syb.moviepedia.tv.external.TmdbTVClient;
import syb.moviepedia.tv.external.dto.TmdbTVCredit;
import syb.moviepedia.tv.repsitory.TVCategoryRepository;
import syb.moviepedia.tv.repsitory.TVRepository;

import java.time.LocalDate;
import java.util.List;

import static syb.moviepedia.tv.domain.QTV.tV;

@Slf4j
@Service
@RequiredArgsConstructor
public class TVService {
    private final TVRepository tvRepo;
    private final TVCategoryRepository tvCategoryRepo;
    private final GenreRepository genreRepo;
    private final CreditRepository creditRepo;
    private final JPAQueryFactory query;

    private final TmdbTVClient tmdbTVClient;

    // TODO: 상세화면 만들기
    // TODO: 영화쪽 TMDB 클래스 한데 묶기
    // TODO: 영화 애니메이션 화수 주기적으로 체크해보기. 화수가 진행되면서 에피소드 카운트가 늘어나는지 확인
    @Transactional
    public List<TVPopularResponse> getPopularTVList() {
        return tvCategoryRepo.findAll().stream().map(category ->
                TVPopularResponse.builder()
                        .code(category.getCode())
                        .title(category.getTitle())
                        .backdrop_path(category.getBackdropPath())
                        .build()).toList();
    }

    @Transactional(readOnly = true)
    public SliceImpl<AllTVsResponse> getAllTV(FilterRequest filter, SortType sortType, Pageable pageable) {
        QTVSeries qSeries = QTVSeries.tVSeries;


        OrderSpecifier<?> orderSpecifier = switch (sortType) {
            case LATEST -> tV.releaseDate.desc();
            case OLDEST -> tV.releaseDate.asc();
        };

        int pageSize = pageable.getPageSize();
        List<TV> tvList = query
                .select(tV)
                .from(tV)
                .leftJoin(tV.series, qSeries).fetchJoin()
                .where(
                        genreExists(qSeries, filter.genre()),
                        releasedCondition(filter.releaseStatus())
                )
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageSize + 1)
                .fetch();


        boolean hasNext = tvList.size() > pageSize;

        if (hasNext) {
            tvList.remove(pageSize);
        }

        List<AllTVsResponse> content = tvList.stream()
                .map(tv -> AllTVsResponse.from(tv))
                .toList();

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageSize,
                switch (sortType) {
                    case LATEST -> Sort.by(Sort.Direction.DESC, "releaseDate");
                    case OLDEST -> Sort.by(Sort.Direction.ASC, "releaseDate");
                }
        );
        return new SliceImpl<>(content, sortedPageable, hasNext);
    }
    // 장르 필터
    private BooleanExpression genreExists(QTVSeries qSeries, List<Integer> genres) {
        if (genres == null || genres.isEmpty()) {
            return null;
        }
        QTVSeriesGenre sg = new QTVSeriesGenre("sg");

        return JPAExpressions
                .selectOne()
                .from(sg)
                .where(
                        sg.tvSeries.eq(qSeries),
                        sg.genre.code.in(genres)
                )
                .exists();
    }

    // 개봉 여부
    private BooleanExpression releasedCondition(ReleaseStatus releaseCond) {
        if (releaseCond == null) {
            return null;
        }
        LocalDate today = LocalDate.now();

        if (releaseCond == ReleaseStatus.RELEASED) {
            return tV.releaseDate.loe(today);
        }

        return tV.releaseDate.gt(today);
    }

    @Transactional
    public List<GenreResponse> getGenres(MediaType mediaType) {

        genreRepo.findAllByMediaType(mediaType);

        return genreRepo.findAllByMediaType(mediaType).stream().map(genre ->
                        GenreResponse.builder()
                                .genreCode(genre.getCode())
                                .name(genre.getName()).build())
                .toList();
    }

    @Transactional(readOnly = true)
    public TVSeasonResponse getSeasonDetail(int seriesCode, int seasonNum) {
        TV tv = tvRepo.findBySeriesCodeAndSeasonNum(seriesCode, seasonNum)
                .orElseThrow(() -> new TVSeasonNotFoundException("TV 시즌 조회 실패"));
        TVSeries series = tv.getSeries();
        List<String> genres = genreRepo.findByGenreCode(series.getGenres());

        return TVSeasonResponse.builder()
                .seasonCode(tv.getSeasonNum())
                .title(series.getTitle())
                .genre(genres)
                .country(series.getCountry())
                .episodeCnt(tv.getEpisodeCnt())
                .releaseDate(tv.getReleaseDate())
                .certification(series.getCertification())
                .posterPath(tv.getPosterPath())
                .overview(tv.getOverview().isEmpty() ? series.getOverview() : tv.getOverview())
                .credit(null)
                .build();
    }

    @Transactional
    public List<TVSeasonCreditResponse> getSeasonCredit(Integer seriesCode, Integer seasonNum) {
        List<Credit> credits = creditRepo.findByMediaTypeAndCodeAndSeasonNum(MediaType.TV, seriesCode, seasonNum);

        // 해당 시즌의 크레딧이 DB에 존재하지 않으면 api 호출 후 저장 및 응답
        if (credits.isEmpty()) {
            TmdbTVCredit tmdbTVCredit = tmdbTVClient.fetchTVSeriesCredits(seriesCode, seasonNum);
            log.info("tmdbTVClient {}", tmdbTVCredit);

            credits.addAll(tmdbTVCredit.crew().stream()
                    .filter(crew -> crew.job().equals(CreditRole.DIRECTOR.getRole()))
                    .map(crew -> Credit.builder()
                            .mediaType(MediaType.MOVIE)
                            .role(CreditRole.DIRECTOR)
                            .code(seriesCode)
                            .name(crew.name())
                            .profile(crew.profile())
                            .castOrder(null).build())
                    .toList());

            // 출연 배우 추출 후 Credit에 넣기
            credits.addAll(tmdbTVCredit.cast().stream()
                    .map(cast -> Credit.builder()
                            .mediaType(MediaType.MOVIE)
                            .role(CreditRole.ACTOR)
                            .code(seriesCode)
                            .name(cast.name())
                            .profile(cast.profile())
                            .castOrder(cast.castOrder())
                            .build())
                    .limit(10) // 출연 배우는 10명만
                    .toList());
            creditRepo.saveAll(credits);
        }

        // 존재하지 않으면 API 호출 후 저장 및 응답

        return credits.stream().map(credit -> TVSeasonCreditResponse.from(credit)).toList();
    }
}
