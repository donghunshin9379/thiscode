package com.example.thiscode.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
// 개발자 도구 favicon 500 에러 방지용
@Controller
public class FaviconController {
    @GetMapping("favicon.ico")
    @ResponseBody
    void returnNoFavicon() {
        // 아무 것도 반환하지 않음
    }
}
