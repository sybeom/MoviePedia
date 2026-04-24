package syb.moviepedia.member.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import syb.moviepedia.common.ProviderType;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false)
    private ProviderType providerType;

    public void update(MemberDto memberDto) {
        this.email = memberDto.getEmail();
        this.nickname = memberDto.getNickname();
    }
}
