package syb.moviepedia.movie.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import syb.moviepedia.movie.service.MovieInitService;

/**
 * 영화 초기 데이터 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MovieDataInitializer implements CommandLineRunner {

    private final MovieInitService movieInitService;

    @Override
    public void run(String... args) {
        log.info("init: {}", movieInitService.init());
    }
}
