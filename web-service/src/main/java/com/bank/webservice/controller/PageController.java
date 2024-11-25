package com.bank.webservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/admin/index")
    public String showAdminIndexPage() {
        //TODO make a separate page for admins
        return "home";
    }

    @GetMapping("/manager/index")
    public String showManagerIndexPage() {
        //TODO make a separate page for managers
        return "home";
    }

    @GetMapping("/user/index")
    public String showUserIndexPage() {
        //TODO make a separate page for users
        return "home";
    }

    @GetMapping("/error")
    public String showErrorPage() {
        return "error";
    }

    @GetMapping("/index")
    public String showLoginPage() {
        return "index";
    }

    @GetMapping("/home")
    public String showHomePage() {
        return "home";
    }

    @GetMapping("/registration")
    public String showRegistrationSuccessfulPage() {
        return "registration-successful";
    }
}