package syb.moviepedia.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import syb.moviepedia.member.domain.Member;
import syb.moviepedia.member.dto.MemberDto;
import syb.moviepedia.member.service.MemberService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberController {

    final private MemberService memberService;

    @PostMapping("/signup")
    public String signup() {
        log.info("signup 호출");
        MemberDto dto = new MemberDto("test", "1234");
        memberService.signup(dto);
        return "Hello World";
    }
}
