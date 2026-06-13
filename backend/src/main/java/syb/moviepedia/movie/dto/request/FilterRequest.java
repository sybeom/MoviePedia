package syb.moviepedia.movie.dto.request;

import syb.moviepedia.common.ReleaseStatus;

import java.util.List;

public record FilterRequest(
        List<Integer> genre,
        ReleaseStatus releaseStatus
) {
}
