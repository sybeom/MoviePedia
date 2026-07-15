package syb.moviepedia.movie.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import syb.moviepedia.movie.service.MovieInitService;
import syb.moviepedia.tv.service.TVInitService;

/**
 * 영화 초기 데이터 클래스 (실행시 1회 실행)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MovieInitService movieInitService;
    private final TVInitService tvInitService;

    @Override
    public void run(String... args) {
        movieInitService.initGenres();
        movieInitService.initCountries();
//        movieInitService.createMember();
//        movieInitService.createComment();
//        movieInitService.initMovies();
//        movieInitService.initCategoryMovies(); // 스케쥴링에서 대신함
//        movieInitService.saveElasticMovies();

//        movieInitService.setGenre();
//        movieInitService.initData();
//        tvInitService.initCategories();
//        tvInitService.initTVGenres();
//        tvInitService.init();
//        tvInitService.initSeries();
        tvInitService.saveElasticTV();
    }
}
