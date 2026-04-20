package syb.moviepedia.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 회원 가입에 필요한 MemberDto
 */
@AllArgsConstructor
@Getter
public class MemberDto {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9]+$")
    @Size(min = 6, max = 10)
    String loginId;

    @NotBlank
    @Size(min = 6, max = 12)
    String password;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9가-힣ㄱ-ㅎ]+$")
    @Size(min = 2, max = 6)
    String nickname;
}
