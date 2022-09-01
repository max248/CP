package com.example.courseproject.Controllers;

import com.example.courseproject.Projections.ItemProjection;
import com.example.courseproject.Repositories.ItemRepository;
import com.example.courseproject.Services.CustomUserDetails;
import com.example.courseproject.Repositories.RoleRepository;
import com.example.courseproject.Repositories.UserRepository;
import com.example.courseproject.model.Items;
import com.example.courseproject.model.Language;
import com.example.courseproject.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    public String loginPage(Model model, String error, String logout, HttpServletRequest request, HttpServletResponse response){
        if (error != null)
            model.addAttribute("errorMsg", "Your username and password are invalid.");

        if (logout != null)
            model.addAttribute("msg", "You have been logged out successfully.");
        String lang = request.getParameter("lang");
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response,new Locale(lang!=null?lang: String.valueOf(Language.en)));
        model.addAttribute("user", new User());
        return "login";
    }

    @GetMapping("/admin_panel")
    public String adminPage(Authentication authentication, HttpServletRequest request){
        CustomUserDetails customUserDetails = authentication != null ? (CustomUserDetails) authentication.getPrincipal() : null;
        request.setAttribute("username",customUserDetails.getFullName());
        return "admin_panel";
    }

    @GetMapping("")
    public String viewHomePage(Authentication authentication, HttpServletRequest request, HttpServletResponse response){
        if(authentication != null && authentication.isAuthenticated()){
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            if(!(userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())){
                authentication.setAuthenticated(false);
                return "login";
            }
            List<Items> listItems = itemRepository.findAllOrderById();
            String projectionList = itemRepository.getItemJsonDataByUserId(customUserDetails.getUserId());
            request.setAttribute("listItems",listItems);
            request.setAttribute("projectionList",projectionList);
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
            localeResolver.setLocale(request, response,new Locale(customUserDetails.getLanguage().toString()));
            return "home";
        } else {
            String lang = request.getParameter("lang");
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
            localeResolver.setLocale(request, response,new Locale(lang!=null?lang: String.valueOf(Language.en)));
            return "index";
        }
    }

    @GetMapping("/home")
    public String homePage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return "redirect:" + "";
    }

}