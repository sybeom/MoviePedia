package syb.moviepedia.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import syb.moviepedia.common.ProviderType;
import syb.moviepedia.member.domain.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    boolean existsByLoginId(String loginId);

    boolean existsByNickname(String nickname);

    Optional<Member> findByLoginId(String loginId);

    Optional<Member> findByNickname(String nickname);

    Optional<Member> findByLoginIdAndProviderTypeNot(String loginId, ProviderType providerType);

    @Query("select m.nickname from Member m where m.loginId = :loginId")
    Optional<String> findNicknameByLoginId(String loginId);
}
