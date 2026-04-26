package syb.moviepedia.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * 회원가입 요청 dto 클래스
 */
@Builder
public record MemberSignupRequestDto(
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9]+$")
        @Size(min = 6, max = 10)
        String loginId,

        @NotBlank
        @Size(min = 2, max = 10)
        String password,

        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9가-힣ㄱ-ㅎ]+$")
        @Size(min = 2, max = 6)
        String nickname
) {}
