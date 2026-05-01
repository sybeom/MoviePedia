package syb.moviepedia.movie.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import syb.moviepedia.movie.external.kobis.KobisBoxOfficeInfo;
import syb.moviepedia.movie.external.kobis.KobisClient;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BoxOfficeService {

    private final KobisClient kobisClient;

    public List<KobisBoxOfficeInfo> getBoxOfficeWeekly() throws JsonProcessingException {
        return kobisClient.fetchWeeklyBoxOffice();
    }
}
