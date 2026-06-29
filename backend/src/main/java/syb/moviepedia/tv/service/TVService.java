package syb.moviepedia.tv.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.common.MediaType;
import syb.moviepedia.movie.dto.response.GenreResponse;
import syb.moviepedia.movie.repository.GenreRepository;
import syb.moviepedia.tv.dto.response.TVPopularResponse;
import syb.moviepedia.tv.repsitory.TVCategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TVService {
    private final TVCategoryRepository tvCategoryRepo;
    private final GenreRepository genreRepo;

    @Transactional
    public List<TVPopularResponse> getPopularTVList() {
        return tvCategoryRepo.findAll().stream().map(category ->
                TVPopularResponse.builder()
                        .code(category.getCode())
                        .title(category.getTitle())
                        .backdrop_path(category.getBackdropPath())
                        .build()).toList();
    }

    public List<GenreResponse> getGenres(MediaType mediaType) {
        genreRepo.findAllByMediaType(mediaType);

        return genreRepo.findAllByMediaType(mediaType).stream().map(genre ->
                        GenreResponse.builder()
                                .genreCode(genre.getCode())
                                .name(genre.getName()).build())
                .toList();
    }
}
