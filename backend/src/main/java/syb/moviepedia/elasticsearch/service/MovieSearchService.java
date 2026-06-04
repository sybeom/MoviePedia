package syb.moviepedia.elasticsearch.service;

import org.springframework.stereotype.Service;
import syb.moviepedia.elasticsearch.domain.MovieDocument;
import syb.moviepedia.elasticsearch.dto.MovieDocRequest;
import syb.moviepedia.elasticsearch.repository.MovieDocumentRepository;

@Service
public class MovieSearchService {

    MovieDocumentRepository movieDocRepository;

    public void save(MovieDocRequest request) {
        MovieDocument movieDoc = MovieDocument.builder()
                .id(request.id())
                .title(request.name())
                .build();

        movieDocRepository.save(movieDoc);
    }
}
