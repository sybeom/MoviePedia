package syb.moviepedia.member.dto;

import lombok.Builder;

@Builder
public record MemberLoginResponseDto(
        String loginId,
        String nickname,
        String accessToken,
        String refreshToken
) {
}
