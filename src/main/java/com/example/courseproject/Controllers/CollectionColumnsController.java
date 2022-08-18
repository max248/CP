package com.example.courseproject.Controllers;

import com.example.courseproject.Repositories.CollectionColumnRepositories;
import com.example.courseproject.model.CollectionColumns;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
public class CollectionColumnsController {

    @Autowired
    private CollectionColumnRepositories collectionColumnRepositories;

    @PostMapping("/get_collection_columns")
    public void getColumnsByCollectionId(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String collection_id = request.getParameter("collection_id");
        List<CollectionColumns> collectionColumnsList = collectionColumnRepositories.findAllByCollection(Long.valueOf(collection_id));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Gson gson = new Gson();
        response.getWriter().write(gson.toJson(collectionColumnsList));
    }
}
