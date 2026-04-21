package syb.moviepedia.member.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Getter
@Builder
@AllArgsConstructor
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
}
