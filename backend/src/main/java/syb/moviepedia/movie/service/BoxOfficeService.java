package syb.moviepedia.movie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import syb.moviepedia.movie.external.KobisClient;

@Slf4j
@RequiredArgsConstructor
@Service
public class BoxOfficeService {

    private final KobisClient kobisClient;

    public String getBoxOfficeWeekly() {
        return kobisClient.getWeeklyBoxOffice();
    }
}
