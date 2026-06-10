package syb.moviepedia.movie.dto.response;

import lombok.Builder;
import syb.moviepedia.common.CreditRole;
import syb.moviepedia.movie.domain.Credit;

/**
 * 영화 출연진 정보 응답 Dto.
 * 영화 상세 응답 Dto의 필드에 list 형태로 들어가 전송된다.
 */
@Builder
public record MovieCreditResponse(
        CreditRole role,
        String name,
        String profile
) {
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/original";

    public MovieCreditResponse {
        profile = profile != null ? IMAGE_BASE_URL + profile : "";
    }

    public static MovieCreditResponse from(Credit credit) {
        return MovieCreditResponse.builder()
                .role(credit.getRole())
                .name(credit.getName())
                .profile(credit.getProfile())
                .build();
    }
}
