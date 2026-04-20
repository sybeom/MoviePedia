package syb.moviepedia.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import syb.moviepedia.member.domain.Member;

public interface MemberRepository extends JpaRepository<Member,Long> {

    boolean existsByLoginId(String loginId);

    boolean existsByNickname(String nickname);
}
