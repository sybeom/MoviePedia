package syb.moviepedia.movie.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import syb.moviepedia.movie.service.MovieInitService;

// TODO: 클래스명 변경하기, 해보니 영화 데이터만 초기화해야하는게 아니다
/**
 * 영화 초기 데이터 클래스 (실행시 1회 실행)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MovieDataInitializer implements CommandLineRunner {

    private final MovieInitService movieInitService;

    @Override
    public void run(String... args) {
        movieInitService.initGenres();
        movieInitService.initCountries();
        movieInitService.createMember();
        movieInitService.createComment();
//        movieInitService.initMovies();
//        movieInitService.initCategoryMovies(); // 스케쥴링에서 대신함
    }
}
