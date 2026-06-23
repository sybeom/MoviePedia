package syb.moviepedia;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import syb.moviepedia.tv.external.TmdbTVClient;
import syb.moviepedia.tv.external.dto.TmdbTVDiscover;

@Slf4j
@SpringBootTest
public class TmdbApiTest {
    @Autowired
    TmdbTVClient tmdbTVClient;

    @Test
    public void tmdbApiTest(){

        TmdbTVDiscover tvTest = tmdbTVClient.getTvTest(1);
        org.assertj.core.api.Assertions.assertThat(tvTest).isNotNull();

        log.info("값확인 {}", tvTest.toString());
    }
}
