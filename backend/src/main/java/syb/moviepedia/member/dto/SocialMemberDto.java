package syb.moviepedia.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 회원가입시 Member 업데이트에 사용된느 DTO
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SocialMemberDto {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9]+$")
    @Size(min = 6, max = 10)
    String loginId;

    @NotBlank
    @Size(min = 2, max = 10)
    String password;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9가-힣ㄱ-ㅎ]+$")
    @Size(min = 2, max = 6)
    String nickname;

    String email;

    public SocialMemberDto(String nickname, String email) {
        this.email = email;
        this.nickname = nickname;
    }
}
