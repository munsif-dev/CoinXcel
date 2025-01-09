package com.munsif.CoinXcel.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/")
    public String home(){
        return "Welcome to Home!";
    }

    @GetMapping("/api")
    public String secure(){
        return "Welcome to Home! Secure";
    }


    @GetMapping("/login")
    public String loginPage() {
        return "Login Page"; // Replace with actual HTML or JSON response
    }

    @GetMapping("/register")
    public String registerPage() {
        return "Register Page"; // Replace with actual HTML or JSON response
    }


}

