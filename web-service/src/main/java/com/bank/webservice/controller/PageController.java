package com.bank.webservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/admin/index")
    public String showAdminIndexPage() {
        return "index";
    }

    @GetMapping("/manager/index")
    public String showManagerIndexPage() {
        return "index";
    }

    @GetMapping("/user/index")
    public String showUserIndexPage() {
        return "index";
    }

    @GetMapping("/error")
    public String showErrorPage() {
        return "error";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "gateway/login";
    }
}