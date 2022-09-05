package com.example.courseproject.Controllers;

import com.example.courseproject.Services.CustomUserDetails;
import com.example.courseproject.Repositories.TopicRepository;
import com.example.courseproject.Repositories.UserRepository;
import com.example.courseproject.Services.FileService;
import com.example.courseproject.model.Language;
import com.example.courseproject.model.Topics;
import com.example.courseproject.model.User;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Controller
public class TopicController {

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FileService fileService;

    @GetMapping("/topic_settings")
    public String viewTopicSettingsPage(Authentication authentication, HttpServletRequest request, Model model, HttpServletResponse response){
        CustomUserDetails customUserDetails = authentication != null ? (CustomUserDetails) authentication.getPrincipal() : null;
        if(!( customUserDetails != null && userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())){
            return "login";
        }
        User user = userRepository.getUserByEmail(customUserDetails.getUsername());
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response,new Locale(user != null ? user.getLanguage().toString() : String.valueOf(Language.en)));
        request.setAttribute("username",customUserDetails.getFullName());
        List<Topics> topicsList = topicRepository.findAllOrderById();
        model.addAttribute("listTopics",topicsList);
        request.setAttribute("role",customUserDetails.getRole().getName());
        return "topic_settings";
    }

    @PostMapping("/add_topic")
    public void addTopic(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            String topicName = request.getParameter("topic_name") != null ? request.getParameter("topic_name") : "";
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
            MultipartFile file = multipartRequest != null?multipartRequest.getFile("image"):null;
            if(topicName.length()>0){
                Topics topic = new Topics();
                User user = new User();
                user = userRepository.findByEmail(customUserDetails.getUsername());
                topic.setName(topicName);
                topic.setStatus(true);
                topic.setUser(user);
                topic.setImageUrl(fileService.getFilePath(file));
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
            List<Topics> listTopics = topicRepository.findAllOrderById();
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
            List<Topics> topicsList = topicRepository.findAllOrderById();
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
            List<Topics> topicsList = topicRepository.findAllOrderById();
            Gson gson = new Gson();
            response.getWriter().write(gson.toJson(topicsList));
        } else {
            response.setContentType("text/html");
            response.getWriter().write("login");
        }
    }
    @PostMapping("/edit_topic")
    public void editTopic(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
        MultipartFile imageFile = multipartRequest != null?multipartRequest.getFile("edit_image"):null;
        if(authentication != null && authentication.isAuthenticated()){
            String name = request.getParameter("edit_name");
            Long id = request.getParameter("id") != null ? Long.valueOf(request.getParameter("id")) : 0;
            Topics topics = topicRepository.getById(id);
            topics.setName(name);
            topics.setUpdateDate(new Date());
            if(imageFile != null)
            topics.setImageUrl(fileService.getFilePath(imageFile));
            topicRepository.save(topics);
            List<Topics> topicsList = topicRepository.findAllOrderById();
            Gson gson = new Gson();
            response.getWriter().write(gson.toJson(topicsList));
        } else {
            response.setContentType("text/html");
            response.getWriter().write("login");
        }
    }
}
