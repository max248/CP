package com.example.courseproject.Controllers;

import com.example.courseproject.CustomUserDetails;
import com.example.courseproject.Repositories.TopicRepository;
import com.example.courseproject.Repositories.UserRepository;
import com.example.courseproject.model.Topics;
import com.example.courseproject.model.User;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class TopicController {

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/topic_settings")
    public String viewTopicSettingsPage(Authentication authentication, HttpServletRequest request, Model model){
        CustomUserDetails customUserDetails = authentication != null ? (CustomUserDetails) authentication.getPrincipal() : null;
        if(!( customUserDetails != null && userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())){
            return "login";
        }
        request.setAttribute("username",customUserDetails.getFullName());
        List<Topics> topicsList = topicRepository.findAll();
        model.addAttribute("listTopics",topicsList);
        return "topic_settings";
    }

    @PostMapping("/add_topic")
    public void addTopic(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            String topicName = request.getParameter("topic_name") != null ? request.getParameter("topic_name") : "";
            if(topicName.length()>0){
                Topics topic = new Topics();
                User user = new User();
                user = userRepository.findByEmail(customUserDetails.getUsername());
                topic.setName(topicName);
                topic.setStatus(true);
                topic.setUser(user);
                Date date = new Date();
                topic.setCreateDate(date);
                if(topicRepository.findByName(topicName) != null && topicRepository.findByName(topicName).getId()>0){
                    response.setContentType("text/html");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("exist");
                    return;
                } else {
                    topicRepository.save(topic);
                }
            } else {
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("error");
                return;
            }
            List<Topics> listTopics = topicRepository.findAll();
            Gson gson = new Gson();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(gson.toJson(listTopics));
        }

    }

    @PostMapping("/delete_topic")
    public void deleteTopic(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if(authentication != null && authentication.isAuthenticated()){
            String delete_ids = request.getParameter("id");
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            for (String id:delete_ids.split(",")){
                topicRepository.deleteById(Long.valueOf(id));
            }
            List<Topics> topicsList = topicRepository.findAll();
            Gson gson = new Gson();
            response.getWriter().write(gson.toJson(topicsList));
        } else {
            response.setContentType("text/html");
            response.getWriter().write("login");
        }
    }
    @PostMapping("/status_topic")
    public void statusTopic(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if(authentication != null && authentication.isAuthenticated()){
            boolean flag = Boolean.parseBoolean(request.getParameter("flag"));
            String delete_ids = request.getParameter("id");
            for (String id:delete_ids.split(",")){
                topicRepository.updateStatusById(Long.valueOf(id),flag);
            }
            List<Topics> topicsList = topicRepository.findAll();
            Gson gson = new Gson();
            response.getWriter().write(gson.toJson(topicsList));
        } else {
            response.setContentType("text/html");
            response.getWriter().write("login");
        }
    }
}
