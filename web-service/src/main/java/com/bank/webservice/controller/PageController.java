package com.bank.webservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

//    @GetMapping("/index")
//    public String showIndexPage() {
//        return "index";
//    }

    @GetMapping("/error")
    public String showErrorPage() {
        return "error";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "gateway/login";
    }
}