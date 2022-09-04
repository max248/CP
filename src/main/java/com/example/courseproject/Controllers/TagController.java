package com.example.courseproject.Controllers;

import com.example.courseproject.Services.CustomUserDetails;
import com.example.courseproject.Repositories.TagRepository;
import com.example.courseproject.Repositories.UserRepository;
import com.example.courseproject.model.Tags;
import com.example.courseproject.model.Topics;
import com.example.courseproject.model.User;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Controller
public class TagController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @GetMapping("/tag_settings")
    public String viewTopicSettingsPage(Authentication authentication, HttpServletRequest request, Model model, HttpServletResponse response){
        CustomUserDetails customUserDetails = authentication != null ? (CustomUserDetails) authentication.getPrincipal() : null;
        if(!( customUserDetails != null && userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())){
            return "login";
        }
        User user = userRepository.getUserByEmail(customUserDetails.getUsername());
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response,new Locale(user.getLanguage().toString()));
        request.setAttribute("username",customUserDetails.getFullName());
        List<Tags> tagsList = tagRepository.findAllOrderById();
        model.addAttribute("tagsList",tagsList);
        request.setAttribute("role",customUserDetails.getRole().getName());
        return "tag_settings";
    }

    @PostMapping("/add_tag")
    public void addTag(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            String topicName = request.getParameter("topic_name") != null ? request.getParameter("topic_name") : "";
            if(topicName.length()>0){
                Tags tag = new Tags();
                User user = new User();
                user = userRepository.findByEmail(customUserDetails.getUsername());
                tag.setName(topicName);
                tag.setStatus(true);
                tag.setUser(user);
                Date date = new Date();
                tag.setCreateDate(date);
                if(tagRepository.findByName(topicName) != null && tagRepository.findByName(topicName).getId()>0){
                    response.setContentType("text/html");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("exist");
                    return;
                } else {
                    tagRepository.save(tag);
                }
            } else {
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("error");
                return;
            }
            List<Tags> listTags = tagRepository.findAllOrderById();
            Gson gson = new Gson();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(gson.toJson(listTags));
        }

    }

    @PostMapping("/delete_tag")
    public void deleteTopic(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if(authentication != null && authentication.isAuthenticated()){
            String delete_ids = request.getParameter("id");
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            for (String id:delete_ids.split(",")){
                tagRepository.deleteById(Long.valueOf(id));
            }
            List<Tags> tagsList = tagRepository.findAllOrderById();
            Gson gson = new Gson();
            response.getWriter().write(gson.toJson(tagsList));
        } else {
            response.setContentType("text/html");
            response.getWriter().write("login");
        }
    }
    @PostMapping("/status_tag")
    public void statusTopic(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if(authentication != null && authentication.isAuthenticated()){
            boolean flag = Boolean.parseBoolean(request.getParameter("flag"));
            String delete_ids = request.getParameter("id");
            for (String id:delete_ids.split(",")){
                tagRepository.updateStatusById(Long.valueOf(id),flag);
            }
            List<Tags> tagsList = tagRepository.findAllOrderById();
            Gson gson = new Gson();
            response.getWriter().write(gson.toJson(tagsList));
        } else {
            response.setContentType("text/html");
            response.getWriter().write("login");
        }
    }

    @PostMapping("/edit_tag")
    public void editTag(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if(authentication != null && authentication.isAuthenticated()){
            String name = request.getParameter("name");
            Long id = request.getParameter("id") != null ? Long.valueOf(request.getParameter("id")) : 0;
            tagRepository.updateNameById(id,name);
            List<Tags> topicsList = tagRepository.findAllOrderById();
            Gson gson = new Gson();
            response.getWriter().write(gson.toJson(topicsList));
        } else {
            response.setContentType("text/html");
            response.getWriter().write("login");
        }
    }

    @PostMapping("/get_tags")
    public void getItem(Authentication authentication, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if(authentication != null && authentication.isAuthenticated()){
            List<Tags> tagsList = tagRepository.findAllOrderById();
            Gson gson = new Gson();
            response.getWriter().write(gson.toJson(tagsList));
        } else {
            response.setContentType("text/html");
            response.getWriter().write("login");
        }
    }
}
