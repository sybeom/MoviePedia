package syb.moviepedia.movie.service;

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
import syb.moviepedia.common.*;
import syb.moviepedia.common.exception.MovieNotFoundException;
import syb.moviepedia.movie.domain.*;
import syb.moviepedia.movie.dto.request.FilterRequest;
import syb.moviepedia.movie.dto.response.*;
import syb.moviepedia.movie.external.tmdb.TmdbClient;
import syb.moviepedia.movie.external.tmdb.dto.*;
import syb.moviepedia.movie.repository.*;
import syb.moviepedia.movie.external.tmdb.dto.TmdbCredit.TmdbCrew;
import syb.moviepedia.movie.external.tmdb.dto.TmdbCredit.TmdbCast;
import syb.moviepedia.movie.external.tmdb.dto.TmdbGenreList.TmdbGenre;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static syb.moviepedia.movie.domain.QMovie.movie;

@Slf4j
@RequiredArgsConstructor
@Service
public class MovieService {
    private final TmdbClient tmdbClient;
    private final MovieRepository mvRepo;
    private final MovieCategoryRepository mvCategoryRepo;
    private final CountryRepository countryRepo;
    private final CreditRepository creditRepo;
    private final VideoRepository videoRepo;
    private final GenreRepository genreRepo;
    private final MovieGenreRepository movieGenreRepo;
    private final JPAQueryFactory query;

    @Transactional(readOnly = true)
    public SliceImpl<AllMoviesResponse> getAllMovies(FilterRequest filter, SortType sortType, Pageable pageable) {
        QMovie qMovie = QMovie.movie;

        OrderSpecifier<?> orderSpecifier = switch (sortType) {
            case LATEST -> qMovie.releaseDate.desc();
            case OLDEST -> qMovie.releaseDate.asc();
        };

        int pageSize = pageable.getPageSize();

        List<Movie> movies = query
                .select(qMovie)
                .from(qMovie)
                .where(
                        genreExists(qMovie, filter.genre()),
                        releasedCondition(filter.releaseStatus())
                )
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageSize + 1)
                .fetch();

        boolean hasNext = movies.size() > pageSize;

        if (hasNext) {
            movies.remove(pageSize);
        }

        List<AllMoviesResponse> content = movies.stream()
                .map(AllMoviesResponse::from)
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
    private BooleanExpression genreExists(QMovie qMovie, List<Integer> genres) {
        if (genres == null || genres.isEmpty()) {
            return null;
        }
        QMovieGenre mg = new QMovieGenre("mg");

        return JPAExpressions
                .selectOne()
                .from(mg)
                .where(
                        mg.movie.eq(qMovie), // 현재 검사할 영화가 해당 장르(필터)를 가지고 있는지 여부
                        mg.genre.code.in(genres)
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
            return movie.releaseDate.loe(today);
        }

        return movie.releaseDate.gt(today);
    }

    @Transactional(readOnly = true)
    public List<MovieBannerResponse> getBannerMovies() {
        List<MovieCategory> mvCategoryList =
                mvCategoryRepo.findByCategoryTypeOrderByPopularity(MediaCategoryType.POPULAR);
        return mvCategoryList.stream().map(mvCategory -> mvCategory.getMovie())
                .map(movie -> MovieBannerResponse.builder()
                        .movieCode(movie.getCode())
                        .title(movie.getTitle())
                        .backdropPath(movie.getBackdropPath())
                        .build())
                .limit(10)
                .toList();
    }

    @Transactional(readOnly = true)
    public MovieCategoriesResponse getCategoryMovies() {
        log.info("영화 엔티티 수: {}", mvRepo.count());
        List<MovieCategory> popularList = mvCategoryRepo.findByCategoryTypeOrderByPopularity(MediaCategoryType.POPULAR);
        List<MovieCategory> upcomingList = mvCategoryRepo.findByCategoryTypeOrderByPopularity(MediaCategoryType.UPCOMING);
        List<MovieCategory> nowPlayingList = mvCategoryRepo.findByCategoryTypeOrderByPopularity(MediaCategoryType.NOW_PLAYING);

        // DTO로 가공
        List<MovieSummaryResponse> popularListDto = popularList.stream().map(res -> toMovieSummaryDto(res)).toList();
        List<MovieSummaryResponse> upcomingListDto = upcomingList.stream().map(res -> toMovieSummaryDto(res)).toList();
        List<MovieSummaryResponse> nowPlayingListDto = nowPlayingList.stream().map(res -> toMovieSummaryDto(res)).toList();

        log.info("Popular movies found: {}", popularList);
        return MovieCategoriesResponse.builder()
                .popular(popularListDto)
                .upcoming(upcomingListDto)
                .nowPlaying(nowPlayingListDto)
                .build();
    }

    // 장르 목록 조회 (필터 목록에 보여질 데이터들)
    @Transactional(readOnly = true)
    public List<GenreResponse> getGenres(MediaType mediaType) {
        log.info("장르 목록 조회 성공");

        return genreRepo.findAllByMediaType(mediaType).stream().map(genre ->
                GenreResponse.builder()
                        .genreCode(genre.getCode())
                        .name(genre.getName()).build())
                .toList();
    }

    /**
     * 영화 상세 정보 가져오기
     * 영화 ID 기준 DB에 없다면 영화 상세 API를 호출하여 추가 상세 정보(국가, 관람등급,런타임 등)을 추가하여 DB 저장.
     * DB에 있더라도 detailFetched가 false면 일부 상세 정보(국가, 관람등급, 런타임 등)이 비어있는 상태이므로 업데이트한다.
     */
    @Transactional
    public MovieDetailResponse getMovieDetail(Integer mvCode) {
        Movie mv = mvRepo.findByCode(mvCode).orElseThrow(() -> new MovieNotFoundException("영화를 찾을 수 없습니다. 영화 코드: " + mvCode));

        // 상세 정보 업데이트 안되어있으면,api 호출 후 업데이트
        if(!mv.isDetailFetched()) {
            log.info("isDetailFetched() 상세 업데이트 진행");
            TmdbMovieDetail detail = tmdbClient.getMovieDetail(mvCode);
            List<String> countries = countryRepo.findNameByCodeIn(detail.country());

            mv.setDetail(
                    detail.title(),
                    detail.releaseDate(),
                    extractCertification(tmdbClient.getMovieCertification(mvCode)),
                    detail.runtime(),
                    countries,
                    detail.overview(),
                    detail.posterPath(),
                    detail.backdropPath()
            );
        }

        // 크레딧(출연) - 없으면 api 호출후 db저장, 있으면 db에서 가져옴
        List<MovieCreditResponse> creditDto = toMovieCreditDto(getCredit(mv));

        // 영화의 코멘트가 20개 이상이면 지수 조회
        int score = 0;
        if (mv.getCommentCount() >= 20) {
            score = mv.getLikeRate();
        }

        // 장르
        List<String> genreNames = movieGenreRepo.findGenresByMovieId(mv.getId())
                .stream()
                .map(Genre::getName)
                .toList();

        return toMovieDetailResponse(mv, genreNames, creditDto, score);
    }

    private void saveMovieGenres(Movie movie, TmdbMovieDetail detail) {
        if (detail.genres() == null || detail.genres().isEmpty()) {
            return;
        }

        // 장르 추출
        List<MovieGenre> movieGenres = detail.genres().stream()
                .map(tmdbGenre -> {
                    Genre genre = genreRepo.findByCode(tmdbGenre.id())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "존재하지 않는 장르입니다. tmdbGenreId=" + tmdbGenre.id()
                            ));

                    return MovieGenre.builder()
                            .movie(movie)
                            .genre(genre)
                            .build();
                })
                .toList();

        movieGenreRepo.saveAll(movieGenres);
    }

    // 출연 배우
    @Transactional
    public List<Credit> getCredit(Movie movie) {
        List<Credit> credits = creditRepo.findByCodeAndMediaType(movie.getCode(), MediaType.MOVIE);

        if (!credits.isEmpty()) {
            log.info("getCast(): 크레딧 정보 존재. 그대로 반환");
            return credits;
        }

        log.info("getCredit(): 크레딧 정보 존재 X, Api 호출후 저장 및 응답 전달");
        // 없으면 크레딧 api 호출 후 MovieCredit 저장, 가져옴
        TmdbCredit tmdbCredit = tmdbClient.getCredit(movie.getCode());
        List<TmdbCrew> crews = tmdbCredit.crew();
        List<TmdbCast> casts = tmdbCredit.cast();

        credits = new ArrayList<>();

        // 감독 추출 후 Credit에 넣기
        credits.addAll(crews.stream()
                .filter(crew -> crew.job().equals(CreditRole.DIRECTOR.getRole()))
                .map(crew -> Credit.builder()
                        .mediaType(MediaType.MOVIE)
                        .role(CreditRole.DIRECTOR)
                        .code(movie.getCode())
                        .name(crew.name())
                        .profile(crew.profile())
                        .castOrder(null).build())
                .toList());

        // 출연 배우 추출 후 Credit에 넣기
        credits.addAll(casts.stream()
                .map(cast -> Credit.builder()
                        .mediaType(MediaType.MOVIE)
                        .role(CreditRole.ACTOR)
                        .code(movie.getCode())
                        .name(cast.name())
                        .profile(cast.profile())
                        .castOrder(cast.castOrder())
                        .build())
                .limit(10) // 출연 배우는 10명만
                .toList());

        log.info("getCast(): 영화 캐스트 api 호출");
        // 없으면 출연 정보 api 호출 후 MovieCast 저장
        return creditRepo.saveAll(credits);
    }

    @Transactional
    public List<VideoResponse> getVideos(Integer movieCode) {

        // 없으면 api 호출 후 DB 저장후 반환
        if(!videoRepo.existsByMediaTypeAndCode(MediaType.MOVIE, movieCode)) {
            log.info("비디오 api 호출후 반환");
            TmdbVideoResponse res = tmdbClient.getVideos(movieCode, "ko-KR");

            if (res.results().isEmpty()) { // 한국판 트레일러가 없다면 영어판 불러오기
                res = tmdbClient.getVideos(movieCode, "en-US");
            }

            saveVideo(res, movieCode);
        }

        // db에 해당 영화의 Video가 이미 존재하면 그대로 반환
        List<Video> videos = videoRepo.findByVideo(MediaType.MOVIE, movieCode, null);
        return toVideoResponse(videos);
    }

    // 영상 저장
    private void saveVideo(TmdbVideoResponse response, Integer movieCode) {
        List<Video> videoList = response.results().stream()
                .filter(result -> // 공식이고 트레일러 또는 티저 영상만. 사이트는 유튜브인 곳만.
                        result.official()
                                && (result.type()== VideoType.TRAILER || result.type() == VideoType.TEASER)
                                && result.site().equals("YouTube"))
                .map(result ->
                        Video.builder()
                                .mediaType(MediaType.MOVIE)
                                .code(movieCode)
                                .key(result.key())
                                .videoType(result.type())
                                .publishedAt(result.publishedAt())
                                .build())
                .toList();
        videoRepo.saveAll(videoList);
    }

    // 응답  가공
    private List<VideoResponse> toVideoResponse(List<Video> videos) {
        return videos.stream().map(video -> VideoResponse.from(video)).toList();
    }

    // 카테고리 영화 -> 영화 요약 DTO 가공
    private MovieSummaryResponse toMovieSummaryDto(MovieCategory mc) {Movie m = mc.getMovie();
        return MovieSummaryResponse.from(m);
    }

    // 영화 상세 정보 -> 영화 엔티티 가공
    private Movie toMovieFromTmdbDetail(TmdbMovieDetail detail, String certification, List<String> countries) {
        return Movie.builder()
                .code(detail.id())
                .title(detail.title())
                .posterPath(detail.posterPath())
                .backdropPath(detail.backdropPath())
                .certification(certification)
                .overview(detail.overview())
                .releaseDate(detail.releaseDate())
                .country(countries)
                .runtime(detail.runtime())
                .build();
    }

    // 영화 상세 DTO로 변환
    private MovieDetailResponse toMovieDetailResponse(Movie m, List<String> genreNames, List<MovieCreditResponse> dto, int score) {
        return MovieDetailResponse.from(m, genreNames, dto, score);
    }

    // Credit 엔티티 영화 크레딧 DTO로 가공
    private List<MovieCreditResponse> toMovieCreditDto(List<Credit> list) {
        return list.stream().map(credit -> MovieCreditResponse.from(credit)).toList();
    }

    // 영화 추가 정보(국가, 런타임 등) 업데이트
    private void updateMovie(Movie movie, TmdbMovieDetail detail) {
        // 상세 api 호출 (국가정보, 런타임)
        List<String> countries = countryRepo.findNameByCodeIn(detail.country()); // 국가 코드 한국어 매핑
        Integer runtime = detail.runtime();

        movie.updateCountryAndRuntime(countries, runtime);
    }

    // 상세 영화 정보 장르 추출
    private List<Integer> extractGenresFromDetail(List<TmdbGenre> genres) {
        return genres.stream().map(genre -> genre.id()).toList();
    }

    // 관람 등급 추출
    private String extractCertification(TmdbMovieCertification response) {
        return response.results().stream()
                .filter(releaseData -> releaseData.iso31661().equals("KR")) // 한국만 추출
                .filter(releaseData -> releaseData.releaseDates() != null)
                .flatMap(releaseDates -> releaseDates.releaseDates().stream()) // release_dates[] 평탄화
                .filter(release -> release.certification() != null && !release.certification().isBlank())
                .filter(release -> release.type() >= 1 && release.type() <=6) // type==3 : 극장 개봉
                .findFirst()
                .map(info -> info.certification())
                .orElse("등급 미정");
    }
}
