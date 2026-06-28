package syb.moviepedia.tv.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syb.moviepedia.tv.dto.response.TVPopularResponse;
import syb.moviepedia.tv.repsitory.TVCategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TVService {
    private final TVCategoryRepository tvCategoryRepo;

    @Transactional
    public List<TVPopularResponse> getPopularTVList() {
        return tvCategoryRepo.findAll().stream().map(category ->
                TVPopularResponse.builder()
                        .code(category.getCode())
                        .title(category.getTitle())
                        .backdrop_path(category.getBackdropPath())
                        .build()).toList();
    }
}
