package syb.moviepedia.movie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import syb.moviepedia.movie.external.tmdb.TmdbClient;

@Slf4j
@RequiredArgsConstructor
@Service
public class MovieService {
    private final TmdbClient tmdbClient;

    public String getPopularMovies() {
        return  tmdbClient.getPopularMovies();
    }
}
