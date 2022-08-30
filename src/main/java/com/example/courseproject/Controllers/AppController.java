package com.example.courseproject.Controllers;

import com.example.courseproject.Projections.ItemProjection;
import com.example.courseproject.Repositories.ItemRepository;
import com.example.courseproject.Services.CustomUserDetails;
import com.example.courseproject.Repositories.RoleRepository;
import com.example.courseproject.Repositories.UserRepository;
import com.example.courseproject.model.Items;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class AppController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ItemRepository itemRepository;

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/login")
    public String loginPage(Model model, String error, String logout){
        if (error != null)
            model.addAttribute("errorMsg", "Your username and password are invalid.");

        if (logout != null)
            model.addAttribute("msg", "You have been logged out successfully.");
        return "login";
    }

    @GetMapping("/admin_panel")
    public String adminPage(Authentication authentication, HttpServletRequest request){
        CustomUserDetails customUserDetails = authentication != null ? (CustomUserDetails) authentication.getPrincipal() : null;
        request.setAttribute("username",customUserDetails.getFullName());
        return "admin_panel";
    }

    @GetMapping("")
    public String viewHomePage(Authentication authentication, HttpServletRequest request){
        if(authentication != null && authentication.isAuthenticated()){
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            if(!(userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())){
                authentication.setAuthenticated(false);
                return "login";
            }
            List<Items> listItems = itemRepository.findAllOrderById();
            List<ItemProjection> projectionList = itemRepository.getItemJsonDataByUserId(customUserDetails.getUserId());
            request.setAttribute("listItems",listItems);
            request.setAttribute("projectionList",projectionList);
            return "home";
        }
        return "index";
    }
}