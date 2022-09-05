package com.example.courseproject.Controllers;

import com.example.courseproject.Services.CustomUserDetails;
import com.example.courseproject.Repositories.*;
import com.example.courseproject.Services.FileService;
import com.example.courseproject.model.*;
import com.example.courseproject.model.Collections;
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
    private ColumnTypeRepository columnTypeRepository;

    @Autowired
    private CollectionColumnRepository collectionColumnRepository;

    @Autowired
    private FileService fileService;
    @GetMapping("/collection_settings")
    public String viewTopicSettingsPage(Authentication authentication, HttpServletRequest request, Model model, HttpServletResponse response){
        CustomUserDetails customUserDetails = authentication != null ? (CustomUserDetails) authentication.getPrincipal() : null;
        if(!( customUserDetails != null && userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())){
            return "login";
        }
        User user = userRepository.getUserByEmail(customUserDetails.getUsername());
        request.setAttribute("username",customUserDetails.getFullName());
        List<Topics> listTopics = topicRepository.findAllOrderById();
        List<Collections> listCollections = collectionRepository.findAllOrderById();
        List<ColumnType> listColumnType= columnTypeRepository.findAll();
        model.addAttribute("listCollections",listCollections);
        model.addAttribute("listTopics",listTopics);
        model.addAttribute("listColumnType",listColumnType);
        request.setAttribute("role",customUserDetails.getRole().getName());
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response,new Locale(user != null ? user.getLanguage().toString() : String.valueOf(Language.en)));
        return "collection_settings";
    }
    @GetMapping("/collection")
    public String viewCollection(Authentication authentication, HttpServletRequest request, HttpServletResponse response){
        Long collectionId = request.getParameter("collection_id") != null? Long.parseLong(request.getParameter("collection_id")) : 0;
        request.setAttribute("collection_id",collectionId);
        Collections collections = collectionRepository.getOne(collectionId);

        request.setAttribute("sign", false);
        request.setAttribute("collection_name", collections.getName());
        request.setAttribute("descriptions", collections.getDescriptions());
        request.setAttribute("author_name", collections.getUser().getFullName());
        request.setAttribute("image_url", collections.getImageUrl());
        if(authentication != null && authentication.isAuthenticated()){
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            if(customUserDetails != null){
                request.setAttribute("role",customUserDetails.getRole().getName());
                request.setAttribute("sign", true);
                User user = userRepository.findByEmail(customUserDetails.getUsername());
                LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
                localeResolver.setLocale(request, response,new Locale(user != null ? user.getLanguage().toString() : String.valueOf(Language.en)));
            }
        }
        return "collection";
    }

    @PostMapping("/add_collection")
    public void addCollection(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            String collectionName = request.getParameter("collection_name") != null ? request.getParameter("collection_name") : "";
            Long topicId = request.getParameter("topicId") != null ? Long.parseLong(request.getParameter("topicId")) : -1;
            String[] typeIds = request.getParameterMap().get("typeId");
            String[] columnNames = request.getParameterMap().get("columnName");
            HashMap<Long,String> columnTypeNames = new HashMap<>();
            User user = new User();
            user = userRepository.findByEmail(customUserDetails.getUsername());
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
            MultipartFile imageFile = multipartRequest != null?multipartRequest.getFile("image"):null;
            if(collectionName.length()>0){
                Collections collections  = new Collections();
                Topics topic = new Topics();
                topic = topicRepository.getById(topicId);
                collections.setUser(user);
                collections.setTopics(topic);
                collections.setName(collectionName);
                collections.setCreateDate(new Date());
                collections.setImageUrl(fileService.getFilePath(imageFile));
                collections.setStatus(true);
                if(collectionRepository.findByName(collectionName) != null && collectionRepository.findByName(collectionName).getId()>0){
                    response.setContentType("text/html");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("exist");
                    return;
                } else {
                    collections = collectionRepository.save(collections);
                    int i=0;
                    for (String id:typeIds) {
                        ColumnType columnType = new ColumnType();
                        Optional<ColumnType> optinalEntity = columnTypeRepository.findById(Long.valueOf(id));
                        columnType = optinalEntity.get();
                        CollectionColumns collectionColumns = new CollectionColumns();
                        collectionColumns.setCollection(collections);
                        collectionColumns.setColumnType(columnType);
                        collectionColumns.setName(columnNames[i]);
                        collectionColumnRepository.save(collectionColumns);
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
                listCollections = collectionRepository.findAllOrderById();
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
                listCollections = collectionRepository.findAllOrderById();
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
                listCollections = collectionRepository.findAllOrderById();
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

    @PostMapping("/edit_collection")
    public void editCollection(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if(authentication != null && authentication.isAuthenticated()){
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
            MultipartFile imageFile = multipartRequest != null?multipartRequest.getFile("edit_image"):null;
            String name = request.getParameter("edit_name");
            Long id = request.getParameter("id") != null ? Long.valueOf(request.getParameter("id")) : 0;
            Optional<Collections> optinalEntity = collectionRepository.findById(id);
            Collections collections = optinalEntity.get();
            collections.setName(name);
            if(imageFile.getSize()>0)
            collections.setImageUrl(fileService.getFilePath(imageFile));
            collections.setUpdateDate(new Date());
            collectionRepository.save(collections);
            List<Collections> collectionsList = collectionRepository.findAllOrderById();
            Gson gson = new Gson();
            response.getWriter().write(gson.toJson(collectionsList));
        } else {
            response.setContentType("text/html");
            response.getWriter().write("login");
        }
    }

    @PostMapping("/get_collection")
    public void getCollection(Authentication authentication, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        if(authentication != null && authentication.isAuthenticated()){
            String projectionList = collectionRepository.getCollectionJsonDataByUserId(customUserDetails.getUserId());
            response.getWriter().write(projectionList);
        } else {
            response.setContentType("text/html");
            response.getWriter().write("login");
        }
    }
}
