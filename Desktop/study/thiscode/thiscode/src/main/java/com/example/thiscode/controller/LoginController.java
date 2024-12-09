package com.example.thiscode.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    // 인증여부
    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            logger.info("인증 정보가 없습니다.");
            return false;
        }
        if (AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            logger.info("사용자가 익명 상태입니다.");
            return false;
        }
        boolean isAuth = authentication.isAuthenticated();
        logger.info("사용자 인증 상태: {}", isAuth);
        return isAuth;
    }

    @GetMapping("/login")
    public String login() {
        logger.info("/login 엔드포인트에 접근했습니다.");
        if (isAuthenticated()) {
            logger.info("사용자가 이미 인증되어 있습니다. /friends로 리다이렉트합니다.");
            return "redirect:/friends";
        }
        logger.info("사용자가 인증되지 않았습니다. 로그인 페이지를 보여줍니다.");
        return "login";
    }

    @GetMapping("/friends")
    public String friends() {
        logger.info("/friends 엔드포인트에 접근했습니다.");
        if (isAuthenticated()) {
            logger.info("사용자가 인증되었습니다. 친구 페이지를 보여줍니다.");
            return "friends";
        }
        logger.info("사용자가 인증되지 않았습니다. /login으로 리다이렉트합니다.");
        return "redirect:/login";
    }
}