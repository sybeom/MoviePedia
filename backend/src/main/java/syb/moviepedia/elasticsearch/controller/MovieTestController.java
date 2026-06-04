package syb.moviepedia.elasticsearch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import syb.moviepedia.elasticsearch.domain.MovieDocument;
import syb.moviepedia.elasticsearch.dto.MovieDocRequest;
import syb.moviepedia.elasticsearch.repository.MovieDocumentRepository;
import syb.moviepedia.elasticsearch.service.MovieSearchService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/movie/search")
public class MovieTestController {

    private final MovieDocumentRepository movieDocumentRepository;
    private final MovieSearchService movieSearchService;

    @PostMapping
    public void createMovie(@RequestBody MovieDocRequest request) {
        movieSearchService.save(request);
    }

    @GetMapping
    public Page<MovieDocument> getUsers() {
        return movieDocumentRepository.findAll(PageRequest.of(0, 10));
    }

    @GetMapping("/{id}")
    public MovieDocument getUser(@PathVariable String id) {
        return movieDocumentRepository.findById(id).get();
    }

    @PutMapping("/{id}")
    public void updateUser(@RequestBody MovieDocRequest dto, @PathVariable String id) {
        MovieDocument movieDoc = movieDocumentRepository.findById(id).get();
        movieDocumentRepository.save(movieDoc);
    }
}
