package syb.moviepedia.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import syb.moviepedia.member.domain.Member;
import syb.moviepedia.member.dto.MemberDto;
import syb.moviepedia.member.service.MemberService;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    final private MemberService memberService;

    @PostMapping
    public ResponseEntity<Void> signup(@Valid @RequestBody MemberDto dto) {

//        // 검증 실패시
//        // 필드 입력 조건에 대한 검증을 하는 것임. 아이디, 비밀번호, 닉네임 등의 중복 체크는 하지 않음)
//        if (bindingResult.hasErrors()) {
//            Set<String> errors = new HashSet<>(); // 필드명, 에러코드 작성
//            bindingResult.getFieldErrors().forEach(error -> {
//                log.info("error: " + error);
//                errors.add(error.getField()); // 에러 필드명과 필드에 대한 기본 에러 메시지 넣음
//            });
//            // 400 응답. 프론트에서 처리.
//            return ResponseEntity.badRequest().build();
//        }
        log.info("signup 호출");
        // 중복 검사 및 가입
        memberService.signup(dto);
        return ResponseEntity.ok().build();
    }
}
