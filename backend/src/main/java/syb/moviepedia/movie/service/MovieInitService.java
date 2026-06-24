package syb.moviepedia.movie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.comment.domain.Comment;
import syb.moviepedia.comment.repository.CommentRepository;
import syb.moviepedia.common.*;
import syb.moviepedia.member.domain.Member;
import syb.moviepedia.member.repository.MemberRepository;
import syb.moviepedia.movie.domain.*;
import syb.moviepedia.movie.external.tmdb.TmdbClient;
import syb.moviepedia.movie.external.tmdb.dto.TmdbGenreList;
import syb.moviepedia.movie.external.tmdb.dto.TmdbMovie;
import syb.moviepedia.movie.external.tmdb.dto.TmdbMovieList;
import syb.moviepedia.movie.repository.*;
import syb.moviepedia.tv.external.TmdbTVClient;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 영화 초기 데이터 설정을 위한 클래스
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MovieInitService {

    private final TmdbTVClient tmdbTVClient;
    private final TmdbClient tmdbClient;
    private final MovieRepository movieRepository;
    private final MovieCategoryRepository movieCategoryRepository;
    private final GenreRepository genreRepository;
    private final CountryRepository countryRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final CommentRepository commentRepository;
    private final ElasticsearchOperations esOperations;
    private final MovieGenreRepository movieGenreRepository;
    private final MovieCreditRepository movieCreditRepository;
    private final VideoRepository videoRepository;
    private static final String INDEX_NAME = "movie_search";
    private static final String TITLE_PATTERN = "^(?!(?=.*\\p{L})(?!.*[가-힣]))[\\p{L}0-9 .,:~!?'\"/(){}\\[\\]&+\\-·]+$";

    // 장르 데이터 초기화(로드)
    public void initGenres() {
        // tmdb 장르 데이터는 총 19개
        if (genreRepository.count() > 0) {
            log.info("initGenres(): 장르 데이터 DB존재");
            return;
        }

        log.info("initGenres() 실행, 장르 데이터 변경 감지");
        List<Genre> genres = tmdbClient.getMovieGenres().genres().stream()
                .filter(genre ->
                        !genreRepository.existsByCode(genre.id())) // 존재하지 않는 값들만
                .map(genre ->
                        Genre.builder()
                                .code(genre.id())
                                .name(genre.name())
                                .build())
                .toList();
        genreRepository.saveAll(genres);
    }

    // 국가 데이터 초기화
    public void initCountries() {
        if (countryRepository.count() > 0) {
            log.info("initCountries(): 국가 데이터 DB존재");
            return;
        }

        log.info("initCountries() 실행, 국가 데이터 변경 감지");
        List<Country> countries = tmdbClient.getCountries().stream()
                .map(country ->
                        Country.builder()
                                .code(country.code())
                                .name(country.name())
                                .build())
                .toList();
        countryRepository.saveAll(countries);
    }

    @Transactional
    public void initMovies(int page) {
        log.info("initMovies() 일반 영화 초기 데이터 호출 페이지: {}", page);
        TmdbMovieList response = tmdbClient.getInitMovies(page);
//        saveMovies(response);
        newSaveMovies(response);
    }


    private void saveMovies(TmdbMovieList responses) {
        List<Integer> codes = responses.results().stream().map(tmdbMovie -> tmdbMovie.code()).toList();
        // 현재 DB에 존재하는 영화 code만 걸러냄, 속도 때문에 Set을 사용
        Set<Long> existingCodes = movieRepository.findCodesByCodeIn(codes);

        List<Movie> movies = responses.results().stream()
                .filter(tmdbMv -> !existingCodes.contains(tmdbMv.code()))// DB에 없는 영화들만
                .filter(tmdbMv ->
                        // 숫자만 와도되고 특수문자만 와도 되지만, 언어가 포함되면 한글이 반드시 최소 1개는 포함하는 정규식
                        tmdbMv.title().matches(TITLE_PATTERN))
                .map(response -> {
                            String certification = extractCertification(response);
                            return toMovie(response, certification);
                })
                .toList();
        movieRepository.saveAll(movies);
//        saveElasticMovies(movies); // 엘라스틱 서치 저장
    }

    @Transactional
    private void newSaveMovies(TmdbMovieList responses) {
        List<TmdbMovie> newMovieResponses = filterNewMovieResponses(responses);

        if (newMovieResponses.isEmpty()) {
            return;
        }

        List<Movie> movies = newMovieResponses.stream()
                .map(response -> {
                    String certification = extractCertification(response);
                    return toMovie(response, certification);
                })
                .toList();

        List<Movie> savedMovies = movieRepository.saveAll(movies);

        saveMovieGenres(newMovieResponses, savedMovies);
    }
    private List<TmdbMovie> filterNewMovieResponses(TmdbMovieList responses) {
        List<Integer> codes = responses.results().stream()
                .map(TmdbMovie::code)
                .toList();

        Set<Long> existingCodes = movieRepository.findCodesByCodeIn(codes);

        return responses.results().stream()
                .filter(tmdbMv -> !existingCodes.contains(tmdbMv.code()))
                .filter(tmdbMv -> tmdbMv.title().matches(TITLE_PATTERN))
                .toList();
    }

    private void saveMovieGenres(
            List<TmdbMovie> newMovieResponses,
            List<Movie> savedMovies
    ) {
        Map<Integer, Movie> movieMap = savedMovies.stream()
                .collect(Collectors.toMap(Movie::getCode, movie -> movie));

        Set<Integer> genreCodes = newMovieResponses.stream()
                .flatMap(response -> response.genres().stream())
                .collect(Collectors.toSet());

        if (genreCodes.isEmpty()) {
            return;
        }

        Map<Integer, Genre> genreMap = genreRepository.findByCodeIn(genreCodes).stream()
                .collect(Collectors.toMap(Genre::getCode, genre -> genre));

        List<MovieGenre> movieGenres = newMovieResponses.stream()
                .flatMap(response -> {
                    Movie movie = movieMap.get(response.code());

                    return response.genres().stream()
                            .map(genreMap::get)
                            .filter(Objects::nonNull)
                            .map(genre -> MovieGenre.builder()
                                    .movie(movie)
                                    .genre(genre)
                                    .build());
                })
                .toList();

        movieGenreRepository.saveAll(movieGenres);
    }

    // 엘라스틱서치에 저장
    @Transactional
    public void saveElasticMovies() {

        Long lastId = 0L;
        int totalIndexedCount = 0;

        while (true) {
            // 1000개씩 조회
            List<Movie> movies = movieRepository.findTop1000ByIdGreaterThanOrderByIdAsc(lastId);

            if (movies.isEmpty()) {
                break;
            }

            List<IndexQuery> queries = movies.stream()
                    .map(movie -> {
                        MovieDocument doc = MovieDocument.from(movie);

                        // 문서 하나를 저장하기 위한 IndexQuery 객체를 만들어주는 빌더
                        // 문서를 ES에 저장하기 위한 요청 객체 (즉 저장 요청 정보)
                        return new IndexQueryBuilder()
                                .withId(doc.getId())
                                .withObject(doc)
                                .build();
                    })
                    .toList();

            esOperations.bulkIndex(queries, IndexCoordinates.of(INDEX_NAME));

            totalIndexedCount += movies.size();

            lastId = movies.get(movies.size() - 1).getId();

            log.info("Elasticsearch 영화 색인 진행 중 - lastId={}, indexedCount={}",
                    lastId,
                    totalIndexedCount
            );
        }
    }

    private void createIndexIfNotExists() {
        // MovieDocument가 사용하는 인덱스를 조작할 수 있는 객체 가져옴
        // MovieDocument 클래스의 애노테이션을 보고 인덱스를 알 수 있음
        // 결국 해당 인덱스를 관리할 수 있는 도구를 꺼낸다는 의미
        IndexOperations idxOp = esOperations.indexOps(MovieDocument.class);

        if (!idxOp.exists()) {
            idxOp.create(); // 인덱스 생성
            idxOp.putMapping(idxOp.createMapping()); // 매핑

            log.info("Elasticsearch movies 인덱스 생성 완료");
        }
    }

    // 카테고리 별 영화 목록 초기화
    public void initCategoryMovies() {
        log.info("initCategoryMovies() 카테고리 영화 초기 데이터 호출");
        if (movieCategoryRepository.count() > 0) {
            return;
        }
        refreshAllCategoryMovies();
    }

    // 카테고리 영화 목록 초기화(저장) 또는 업데이트
    private void refreshCategoryMovies(MovieCategoryType category, TmdbMovieList responses) {
        log.info("RefreshCategoryMovies 호출 : {}", category);
        // 기존 카테고리 영화 모두 삭제 (카테고리 목록들을 모두 삭제하고 다시 저장하는 방식으로 갱신한다)
        movieCategoryRepository.deleteByCategoryType(category);

        // 카테고리별 영화 새로 갱신
        for (TmdbMovie response: responses.results()) {
            if (!response.title().matches(TITLE_PATTERN))
                continue;

            log.info("refreshCategoryMovies 초기화 시작 : {}", category);
            Movie movie = saveOrUpdateMovie(response); // 영화 DB에 영화가 존재하면 갱신, 없다면 저장

            MovieCategory mc = MovieCategory.builder()
                    .categoryType(category)
                    .movie(movie)
                    .popularity(response.popularity())
                    .build();

            movieCategoryRepository.save(mc);
        }
    }

    // 스케줄링에 따른 카테고리 영화 초기화
    @Transactional
    public void refreshAllCategoryMovies() {
        log.info("카테고리 영화 전체 갱신 시작");
        refreshCategoryMovies(MovieCategoryType.POPULAR, tmdbClient.getPopularMovies());
        refreshCategoryMovies(MovieCategoryType.UPCOMING, tmdbClient.getUpcomingMovies());
        refreshCategoryMovies(MovieCategoryType.NOW_PLAYING, tmdbClient.getNowPlayingMovies());
        log.info("카테고리 영화 전체 갱신 완료");
    }


    // TmdbMovie -> Movie 엔티티로 가공
    private Movie toMovie(TmdbMovie tmdbMovie, String certification) {
        log.info("영화 제목 : {}", tmdbMovie.title());
        return Movie.builder()
                .code(tmdbMovie.code())
                .title(tmdbMovie.title())
                .backdropPath(tmdbMovie.backdropPath())
                .posterPath(tmdbMovie.posterPath())
                .certification(certification)
                .overview(tmdbMovie.overview())
                .releaseDate(tmdbMovie.releaseDate())
                .country(tmdbMovie.country())
                .runtime(tmdbMovie.runtime())
                .detailFetched(false)
                .build();
    }

    // 영화 저장 또는 갱신
    private Movie saveOrUpdateMovie(TmdbMovie tmdbMovie) {
        String certification = extractCertification(tmdbMovie); // 관람 등급

        return movieRepository.findByCode(tmdbMovie.code()) // 영화 code에 해당하는 영화 찾기
                .map(movie -> {
                    log.info("saveOrUpdateMovie(): 영화 정보 갱신");
                    movie.updateFrom(tmdbMovie, certification); // 존재하면 영화 정보 업데이트(값들이 변경되어있을 수 있기때문)
                    return movie;})
                .orElseGet(() -> movieRepository.save(toMovie(tmdbMovie, certification))); // 존재하지 않으면 db 저장
    }

    // 관람 등급 추출
    private String extractCertification(TmdbMovie tmdbMovie) {
        return tmdbClient.getMovieCertification(tmdbMovie.code()).results().stream()
                .filter(releaseData -> releaseData.iso31661().equals("KR")) // 한국만 추출
                .filter(releaseData -> releaseData.releaseDates() != null)
                .flatMap(releaseDates -> releaseDates.releaseDates().stream()) // release_dates[] 평탄화
                .filter(release -> release.certification() != null)
                .filter(release -> release.type() == 3) // type==3 : 극장 개봉
                .map(release -> release.certification())
                .filter(certification -> certification != null && !certification.isBlank())
                .findFirst()
                .orElse("등급 미정"); // 위 필터들을 통과하지 못하면 등급 미지정
    }

    // 더미 회원 생성
    public void createMember() {
        if (memberRepository.count() > 0)
            return;

        log.info("createMember(): 더미 멤버 생성 시작");
        List<Member> list = new ArrayList<>();

        for (int i = 9; i < 100; i++) {
            Member member = Member.builder()
                    .loginId("test" + i)
                    .password(passwordEncoder.encode("1234"))
                    .nickname("test" + i)
                    .email(null)
                    .role(RoleType.USER)
                    .providerType(ProviderType.LOCAL)
                    .build();
            list.add(member);
        }
        memberRepository.saveAll(list);
    }

    // 더미 코멘트 생성
    public void createComment() {
        if (commentRepository.count() > 0) return;

        log.info("createComment(): 코멘트 더미 데이터 생성 시작");
        Movie movie = movieRepository.findByCode(350).get();

        List<Comment> list = new ArrayList<>();

        for (int i = 7; i < 100; i++) {
            Member member = memberRepository.findByLoginId("test" + i).get();
            ReactionType reactionType = Math.random() < 0.5 ? ReactionType.LIKE : ReactionType.DISLIKE;
            Comment comment = Comment.builder()
                    .nickname(member.getNickname())
                    .content("test" + i)
                    .movie(movie)
                    .reactionType(reactionType)
                    .member(member)
                    .build();
            list.add(comment);
            movie.increaseCommentStats(reactionType); // 코멘트 수, 좋아요 수 업데이트
        }
        commentRepository.saveAll(list);
    }
}
