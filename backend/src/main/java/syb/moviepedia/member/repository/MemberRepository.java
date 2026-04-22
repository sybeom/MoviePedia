package syb.moviepedia.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import syb.moviepedia.member.domain.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    boolean existsByLoginId(String loginId);

    boolean existsByNickname(String nickname);

    Optional<Member> findByLoginId(String loginId);

    Optional<String> findByNickname(String nickname);
}
