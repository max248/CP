package com.example.courseproject.Controllers;

import com.example.courseproject.Repositories.CommentRepository;
import com.example.courseproject.Repositories.ItemRepository;
import com.example.courseproject.Repositories.UserRepository;
import com.example.courseproject.Services.CustomUserDetails;
import com.example.courseproject.model.Comments;
import com.example.courseproject.model.User;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Controller
public class CommentController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ItemRepository itemRepository;

    @PostMapping("/get_comments")
    public void getItem(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Long itemId = request.getParameter("item_id") != null ? Long.parseLong(request.getParameter("item_id")) : 0;
        List<Comments> commentsList = commentRepository.findAllbyItemId(itemId);
        Gson gson = new Gson();
        response.getWriter().write(gson.toJson(commentsList));
    }
    @PostMapping("/add_comment")
    public void addComment(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            if (!(userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())) {
                authentication.setAuthenticated(false);
                response.getWriter().write("login");
                return;
            }
            String content = request.getParameter("content") != null ? request.getParameter("content") : "";
            Long itemId = request.getParameter("item_id") != null ? Long.parseLong(request.getParameter("item_id")) : null;
            User user = userRepository.findByEmail(customUserDetails.getUsername());
            Comments comments = new Comments();
            if(itemId != null){
                comments.setItem(itemRepository.getOne(itemId));
                comments.setContent(content);
                Date date = new Date();
                comments.setCreateDate(date);
                comments.setUser(user);
                commentRepository.save(comments);
            }
            response.getWriter().write("success");
        } else {
            response.getWriter().write("login");
        }
    }
}
