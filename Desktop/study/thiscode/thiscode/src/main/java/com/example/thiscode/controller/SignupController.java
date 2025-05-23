package com.example.thiscode.controller;

import com.example.thiscode.domain.Member;
import com.example.thiscode.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class SignupController {

    private final MemberService memberService;
    private static final Logger logger = LoggerFactory.getLogger(SignupController.class);

    public SignupController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("signup")
    public String signup() {
        return "signup";
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody Member member) {
        try {
            memberService.save(member);
            return ResponseEntity.ok("{\"message\":\"회원가입 성공\"}");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"이미 존재하는 회원입니다.\"}");
        }
    }

    // 이메일 중복
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam("email") String email) {
        boolean exists = memberService.existsByEmail(email); // 이메일 중복 체크
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
}
