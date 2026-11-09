package com.ueb.hotel.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/app/reservations";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /** Çdo metodë: Spring Security e përcjell këtu edhe kërkesat POST të ndaluara. */
    @RequestMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
