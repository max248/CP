package com.example.courseproject.Controllers;

import com.example.courseproject.CustomUserDetails;
import com.example.courseproject.RoleRepository;
import com.example.courseproject.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class AppController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/login")
    public String loginPage(Model model, String error, String logout){
        if (error != null)
            model.addAttribute("errorMsg", "Your username and password are invalid.");

        if (logout != null)
            model.addAttribute("msg", "You have been logged out successfully.");
        return "login";
    }

    @GetMapping("")
    public String viewHomePage(Authentication authentication){
        if(authentication != null && authentication.isAuthenticated()){
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            if(!(userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())){
                authentication.setAuthenticated(false);
                return "login";
            }
            Date date = new Date();
            userRepository.updateLastLoginDateById(customUserDetails.getUserId(),formatter.format(date));
            return "home";
        }
        return "index";
    }
}