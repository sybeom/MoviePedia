package syb.moviepedia.tv.dto.response;

import lombok.Builder;
import syb.moviepedia.common.CreditRole;
import syb.moviepedia.movie.domain.Credit;

@Builder
public record TVSeasonCreditResponse(
        CreditRole role,
        String name,
        String profile
) {
    public static TVSeasonCreditResponse from(Credit credit) {
        return TVSeasonCreditResponse.builder()
                .role(credit.getRole())
                .name(credit.getName())
                .profile(credit.getProfile())
                .build();
    }
}
