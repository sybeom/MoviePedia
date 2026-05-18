package syb.moviepedia.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 가입에 필요한 MemberDto
 * 이 클래스는 소셜에서 쓰인다.
 */
// TODO: 추후 요청,응답 Dto 별도로 분리하도록 한다.
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MemberDto {

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

    public MemberDto(String nickname, String email) {
        this.email = email;
        this.nickname = nickname;
    }
}
