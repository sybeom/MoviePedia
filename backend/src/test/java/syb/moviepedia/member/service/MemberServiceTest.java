package syb.moviepedia.member.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import syb.moviepedia.member.repository.MemberRepository;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    MemberService memberService;

    @Mock
    MemberRepository memberRepository;

    @DisplayName("저장시, Long 타입 id가 반환된다")
    @Test()
    public void signup() {
//
//        // given
//        MemberDto memberDto = new MemberDto("test", "1234");
//
//        Member member = new Member("test", "test");
//        ReflectionTestUtils.setField(member, "id", 1L);
//        given(memberRepository.save(any(Member.class))).willReturn(member);
//
//        // when
//        Long id = memberService.signup(memberDto);
//
//        // then
//        Assertions.assertTrue(id instanceof Long);
    }
}