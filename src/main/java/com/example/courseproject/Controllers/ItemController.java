package com.example.courseproject.Controllers;

import com.example.courseproject.Services.CustomUserDetails;
import com.example.courseproject.Repositories.*;
import com.example.courseproject.model.*;
import com.google.gson.Gson;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

    @PostMapping("/add_item")
    public void addTopic(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = new User();
            user = userRepository.findByEmail(customUserDetails.getUsername());
            String itemName = request.getParameter("itemName") != null ? request.getParameter("itemName") : "";
            String collectionId = request.getParameter("collectionId") != null ? request.getParameter("collectionId") : "";
            String collectionColumnValues = request.getParameter("collectionColumnValues") != null ? request.getParameter("collectionColumnValues") : "";
            String tags = request.getParameter("tags") != null ? request.getParameter("tags") : "";
            Gson gson = new Gson();
            JsonModel[] jsonModel = gson.fromJson(collectionColumnValues.toString(),JsonModel[].class);


            if(itemName.length()>0){
                Optional<Collections> optionalCollections = collectionRepository.findById(Long.valueOf(collectionId));
                Collections collections = optionalCollections.get();
                Items items = new Items();
                items.setCollection(collections);
                items.setCreateDate(new Date());
                items.setUser(user);
                items.setStatus(true);
                items.setName(itemName);
                if(itemRepository.findByName(itemName, user.getId()) != null && itemRepository.findByName(itemName, user.getId()).getId()>0){
                    response.setContentType("text/html");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("exist");
                    return;
                } else {
                    items = itemRepository.save(items);
                    for(int i=0;i<jsonModel.length;i++){
                        List<CollectionColumns> collectionColumns = collectionColumnRepository.findByNameCollection(collections.getId(),jsonModel[i].getLabel());
                        if(collectionColumns.size()>0){
                            ItemData itemData = new ItemData();
                            itemData.setItem(items);
                            itemData.setCollectionColumns(collectionColumns.get(0));
                            itemData.setData(jsonModel[i].getVal());
                            itemDataRepository.save(itemData);
                        }
                    }

                    for (String tag: tags.split(",")) {
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

}
