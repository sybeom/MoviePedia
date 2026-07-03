package syb.moviepedia.tv.service;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.common.MediaType;
import syb.moviepedia.common.ReleaseStatus;
import syb.moviepedia.common.SortType;
import syb.moviepedia.movie.dto.request.FilterRequest;
import syb.moviepedia.movie.dto.response.GenreResponse;
import syb.moviepedia.movie.repository.GenreRepository;
import syb.moviepedia.tv.domain.*;
import syb.moviepedia.tv.dto.response.AllTVsResponse;
import syb.moviepedia.tv.dto.response.TVPopularResponse;
import syb.moviepedia.tv.repsitory.TVCategoryRepository;

import java.time.LocalDate;
import java.util.List;

import static syb.moviepedia.movie.domain.QMovie.movie;
import static syb.moviepedia.tv.domain.QTV.tV;

@Service
@RequiredArgsConstructor
public class TVService {
    private final TVCategoryRepository tvCategoryRepo;
    private final GenreRepository genreRepo;
    private final JPAQueryFactory query;

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
        QTVSeries qTVSeries = QTVSeries.tVSeries;


        OrderSpecifier<?> orderSpecifier = switch (sortType) {
            case LATEST -> tV.releaseDate.desc();
            case OLDEST -> tV.releaseDate.asc();
        };

        int pageSize = pageable.getPageSize();
        List<TV> tvList = query
                .select(tV)
                .from(tV)
                .join(qTVSeries).on(tV.code.eq(qTVSeries.code)) // TV와 시리즈 연관관계가 없어서 필요함
                .where(
                        genreExists(qTVSeries, filter.genre()),
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
}
