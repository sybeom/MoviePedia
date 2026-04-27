package syb.moviepedia.member.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import syb.moviepedia.common.ProviderType;
import syb.moviepedia.common.RoleType;
import syb.moviepedia.member.dto.MemberDto;

@Slf4j
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    String loginId;

    @Column(unique = true, nullable = false)
    String password;

    @Column(unique = true, nullable = false)
    String nickname;

    @Column(unique = true)
    String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING) // 해당 설정 값이 없으면 Enum 값이 0,1 이렇게 순서대로 들어감. 이것은 문자 그대로 들어감
    RoleType role;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false)
    private ProviderType providerType;

    public void update(MemberDto memberDto) {
        this.email = memberDto.getEmail();
        this.nickname = memberDto.getNickname();
    }
}
