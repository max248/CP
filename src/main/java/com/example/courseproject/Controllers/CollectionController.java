package com.example.courseproject.Controllers;

import com.example.courseproject.CustomUserDetails;
import com.example.courseproject.Repositories.*;
import com.example.courseproject.model.*;
import com.example.courseproject.model.Collections;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Controller
public class CollectionController {

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private ColumnTypeRepositories columnTypeRepositories;

    @Autowired
    private CollectionColumnRepositories collectionColumnRepositories;

    @GetMapping("/collection_settings")
    public String viewTopicSettingsPage(Authentication authentication, HttpServletRequest request, Model model){
        CustomUserDetails customUserDetails = authentication != null ? (CustomUserDetails) authentication.getPrincipal() : null;
        if(!( customUserDetails != null && userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())){
            return "login";
        }
        request.setAttribute("username",customUserDetails.getFullName());
        List<Topics> listTopics = topicRepository.findAll();
        List<Collections> listCollections = collectionRepository.findAll();
        List<ColumnType> listColumnType= columnTypeRepositories.findAll();
        model.addAttribute("listCollections",listCollections);
        model.addAttribute("listTopics",listTopics);
        model.addAttribute("listColumnType",listColumnType);
        return "collection_settings";
    }

    @PostMapping("/add_collection")
    public void addCollection(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            String collectionName = request.getParameter("collection_name") != null ? request.getParameter("collection_name") : "";
            String typeIds = request.getParameter("typeIds") != null ? request.getParameter("typeIds") : "";
            String columnNames = request.getParameter("columnNames") != null ? request.getParameter("columnNames") : "";
            Long topicId = request.getParameter("topicId") != null ? Long.parseLong(request.getParameter("topicId")) : -1;
            HashMap<Long,String> columnTypeNames = new HashMap<>();
            User user = new User();
            user = userRepository.findByEmail(customUserDetails.getUsername());
            if(collectionName.length()>0){
                Collections collections  = new Collections();
                Topics topic = new Topics();
                topic = topicRepository.getById(topicId);
                Date date = new Date();

                collections.setUser(user);
                collections.setTopics(topic);
                collections.setName(collectionName);
                collections.setCreateDate(date);
                if(collectionRepository.findByName(collectionName) != null && collectionRepository.findByName(collectionName).getId()>0){
                    response.setContentType("text/html");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("exist");
                    return;
                } else {
                    collections = collectionRepository.save(collections);
                    int i=0;
                    for (String typeId:typeIds.split(",")) {
                        ColumnType columnType = new ColumnType();
                        Optional<ColumnType> optinalEntity = columnTypeRepositories.findById(Long.valueOf(typeId));
                        columnType = optinalEntity.get();
                        CollectionColumns collectionColumns = new CollectionColumns();
                        collectionColumns.setCollection(collections);
                        collectionColumns.setColumnType(columnType);
                        collectionColumns.setName(columnNames.split(",")[i]);
                        collectionColumnRepositories.save(collectionColumns);
                        i++;
                    }

                }
            } else {
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("error");
                return;
            }

            List<Collections> listCollections = new ArrayList<>();
            if(user.getRole().getName().equals("ADMIN")){
                listCollections = collectionRepository.findAll();
            } else if(user.getRole().getName().equals("USER")){
                listCollections = collectionRepository.findAllByUser(user.getId());
            }
            Gson gson = new Gson();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(gson.toJson(listCollections));
        }

    }

    @PostMapping("/delete_collection")
    public void deleteCollection(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if(authentication != null && authentication.isAuthenticated()){
            String delete_ids = request.getParameter("id");
            for (String id:delete_ids.split(",")){
                collectionRepository.deleteById(Long.valueOf(id));
            }
            User user = new User();
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            user = userRepository.findByEmail(customUserDetails.getUsername());
            List<Collections> listCollections = new ArrayList<>();
            if(user.getRole().getName().equals("ADMIN")){
                listCollections = collectionRepository.findAll();
            } else if(user.getRole().getName().equals("USER")){
                listCollections = collectionRepository.findAllByUser(user.getId());
            }
            Gson gson = new Gson();
            response.getWriter().write(gson.toJson(listCollections));
        } else {
            response.setContentType("text/html");
            response.getWriter().write("login");
        }
    }

    @PostMapping("/status_collection")
    public void statusCollection(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if(authentication != null && authentication.isAuthenticated()){
            boolean flag = Boolean.parseBoolean(request.getParameter("flag"));
            String delete_ids = request.getParameter("id");
            for (String id:delete_ids.split(",")){
                collectionRepository.updateStatusById(Long.valueOf(id),flag);
            }
            User user = new User();
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            user = userRepository.findByEmail(customUserDetails.getUsername());
            List<Collections> listCollections = new ArrayList<>();
            if(user.getRole().getName().equals("ADMIN")){
                listCollections = collectionRepository.findAll();
            } else if(user.getRole().getName().equals("USER")){
                listCollections = collectionRepository.findAllByUser(user.getId());
            }
            Gson gson = new Gson();
            response.getWriter().write(gson.toJson(listCollections));
        } else {
            response.setContentType("text/html");
            response.getWriter().write("login");
        }
    }


}
