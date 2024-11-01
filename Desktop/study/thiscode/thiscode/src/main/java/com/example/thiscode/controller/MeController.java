package com.example.thiscode.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MeController {

    @GetMapping("friends")
    public String friends() {
        return "friends";
    }
}
