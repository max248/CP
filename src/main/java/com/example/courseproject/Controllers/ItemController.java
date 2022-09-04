package com.example.courseproject.Controllers;

import com.example.courseproject.Projections.ItemProjection;
import com.example.courseproject.Services.CustomUserDetails;
import com.example.courseproject.Repositories.*;
import com.example.courseproject.Services.FileService;
import com.example.courseproject.model.*;
import com.example.courseproject.model.Collections;
import com.google.gson.Gson;
import org.apache.commons.lang3.math.NumberUtils;
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
public class ItemController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private CollectionColumnRepository collectionColumnRepository;

    @Autowired
    private ItemDataRepository itemDataRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ItemTagsRepository itemTagsRepository;
    @Autowired
    private FileService fileService;
    @GetMapping("/item_settings")
    public String viewItemSettingsPage(Authentication authentication, HttpServletRequest request, Model model){
        CustomUserDetails customUserDetails = authentication != null ? (CustomUserDetails) authentication.getPrincipal() : null;
        if(!( customUserDetails != null && userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())){
            return "login";
        }
        request.setAttribute("username",customUserDetails.getFullName());
        List<Items> itemsList = itemRepository.findAll();
        List<Collections> collectionsList = collectionRepository.findAll();
        List<Tags> tagsList = tagRepository.findAll();
        model.addAttribute("listItems",itemsList);
        model.addAttribute("listCollections",collectionsList);
        model.addAttribute("listTags",tagsList);
        return "item_settings";
    }
    @GetMapping("/item")
    public String viewItem(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String itemId = request.getParameter("item_id") != null? request.getParameter("item_id") : "0";
        request.setAttribute("item_id",itemId);

        if(authentication != null && authentication.isAuthenticated()){
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            if(customUserDetails != null){
                request.setAttribute("role",customUserDetails.getRole().getName());
                request.setAttribute("sign", true);
                User user = userRepository.findByEmail(customUserDetails.getUsername());
                LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
                localeResolver.setLocale(request, response,new Locale(user.getLanguage().toString()));
            }
        }
        return "item";
    }

    @PostMapping("/add_item")
    public void addItem(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = new User();
            user = userRepository.findByEmail(customUserDetails.getUsername());
            String itemName = request.getParameter("item_name") != null ? request.getParameter("item_name") : "";
            String[] collectionId = request.getParameterMap().get("collectionId");
            String[] collectionColumnValues = request.getParameterMap().get("collectionColumn");
            String[] collectionColumnValueIds = request.getParameterMap().get("collectionColumnId");
            String[] tags = request.getParameterMap().get("tags");
            Gson gson = new Gson();
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
            MultipartFile imageFile = multipartRequest != null?multipartRequest.getFile("image"):null;
            if(itemName.length()>0){
                Optional<Collections> optionalCollections = collectionRepository.findById(Long.valueOf(collectionId[0]));
                Collections collections = optionalCollections.get();
                Items items = new Items();
                items.setCollection(collections);
                items.setCreateDate(new Date());
                items.setUser(user);
                items.setStatus(true);
                items.setName(itemName);
                items.setImageUrl(fileService.getFilePath(imageFile));
                if(itemRepository.findByName(itemName, user.getId()) != null && itemRepository.findByName(itemName, user.getId()).getId()>0){
                    response.setContentType("text/html");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("exist");
                    return;
                } else {
                    items = itemRepository.save(items);
                    for (int i=0;i<collectionColumnValueIds.length;i++){
                        Optional<CollectionColumns> optionalCollectionColumns = collectionColumnRepository.findById(Long.valueOf(collectionColumnValueIds[i]));
                        ItemData itemData = new ItemData();
                        itemData.setItem(items);
                        itemData.setCollectionColumns(optionalCollectionColumns.get());
                        if(collectionColumnValues.length>i)
                        itemData.setData(collectionColumnValues[i]);
                        itemDataRepository.save(itemData);
                    }

                    for (String tag: tags) {
                        Tags tags1 = new Tags();
                        if(isNumeric(tag)){
                            Optional<Tags> optionalTags = tagRepository.findById(Long.valueOf(tag));
                            if(optionalTags != null && optionalTags.get() != null && optionalTags.get().getId()>0){
                                tags1 = optionalTags.get();
                            } else {
                                tags1.setName(tag);
                                tags1.setCreateDate(new Date());
                                tags1.setUser(user);
                                tags1.setStatus(true);
                                tags1 = tagRepository.save(tags1);
                            }
                        } else {
                            tags1.setName(tag);
                            tags1.setCreateDate(new Date());
                            tags1.setUser(user);
                            tags1.setStatus(true);
                            tags1 = tagRepository.save(tags1);
                        }
                        ItemTags itemTags = new ItemTags();
                        itemTags.setItems(items);
                        itemTags.setTags(tags1);
                        itemTags.setCreateDate(new Date());
                        itemTagsRepository.save(itemTags);
                    }
                }
            } else {
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("error");
                return;
            }
            List<Items> listItems = itemRepository.findAll();
            gson = new Gson();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(gson.toJson(listItems));
        }

    }
    public static boolean isNumeric(final String str) {
        return NumberUtils.isDigits(str);
    }
    @PostMapping("/delete_item")
    public void deleteItem(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if(authentication != null && authentication.isAuthenticated()){
            String delete_ids = request.getParameter("id");
            for (String id:delete_ids.split(",")){
                itemRepository.deleteById(Long.valueOf(id));
            }
            User user = new User();
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            user = userRepository.findByEmail(customUserDetails.getUsername());
            List<Items> listItems = new ArrayList<>();
            if(user.getRole().getName().equals("ADMIN")){
                listItems = itemRepository.findAllOrderById();
            } else if(user.getRole().getName().equals("USER")){
                listItems = itemRepository.findAllByUser(user.getId());
            }
            Gson gson = new Gson();
            response.getWriter().write(gson.toJson(listItems));
        } else {
            response.setContentType("text/html");
            response.getWriter().write("login");
        }
    }
    @PostMapping("/status_item")
    public void statusItem(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if(authentication != null && authentication.isAuthenticated()){
            boolean flag = Boolean.parseBoolean(request.getParameter("flag"));
            String delete_ids = request.getParameter("id");
            for (String id:delete_ids.split(",")){
                itemRepository.updateStatusById(Long.valueOf(id),flag);
            }
            User user = new User();
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            user = userRepository.findByEmail(customUserDetails.getUsername());
            List<Items> listItems = new ArrayList<>();
            if(user.getRole().getName().equals("ADMIN")){
                listItems = itemRepository.findAllOrderById();
            } else if(user.getRole().getName().equals("USER")){
                listItems = itemRepository.findAllByUser(user.getId());
            }
            Gson gson = new Gson();
            response.getWriter().write(gson.toJson(listItems));
        } else {
            response.setContentType("text/html");
            response.getWriter().write("login");
        }
    }
    @PostMapping("/edit_item")
    public void editItem(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if(authentication != null && authentication.isAuthenticated()){
            String name = request.getParameter("edit_name");
            Long id = request.getParameter("id") != null ? Long.valueOf(request.getParameter("id")) : 0;
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
            MultipartFile imageFile = multipartRequest != null?multipartRequest.getFile("edit_image"):null;
            Optional<Items> optionalItems = itemRepository.findById(id);
            Items items = optionalItems.get();
            items.setName(name);
            if(imageFile.getSize()>0)
            items.setImageUrl(fileService.getFilePath(imageFile));
            items.setUpdateDate(new Date());
            itemRepository.save(items);
            List<Items> collectionsList = itemRepository.findAllOrderById();
            Gson gson = new Gson();
            response.getWriter().write(gson.toJson(collectionsList));
        } else {
            response.setContentType("text/html");
            response.getWriter().write("login");
        }
    }
    @PostMapping("/get_items")
    public void getItem(Authentication authentication, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if(authentication != null && authentication.isAuthenticated()){
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            String projectionList = itemRepository.getItemJsonDataByUserId(customUserDetails.getUserId());
            response.getWriter().write(projectionList);
        } else {
            String projectionList = itemRepository.getItemJsonDataByUserId(null);
            response.getWriter().write(projectionList);
        }
    }

    @PostMapping("/get_item_id")
    public void getItemById(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String itemId = request.getParameter("item_id");
        String projectionList = itemRepository.getItemJsonDataByItemId(Long.valueOf(itemId));
        response.getWriter().write(projectionList);
    }

}
