package com.crowdquery.crowdquery.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() 
            && !authentication.getName().equals("anonymousUser")) {
            return "redirect:/dashboard.html";
        }
        return "redirect:/login.html";
    }
    
    @GetMapping("/login")
    public String login() {
        return "redirect:/login.html";
    }
}
