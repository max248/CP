package com.example.courseproject.Controllers;

import com.example.courseproject.Repositories.ItemRepository;
import com.example.courseproject.Repositories.RateRepository;
import com.example.courseproject.Repositories.UserRepository;
import com.example.courseproject.Services.CustomUserDetails;
import com.example.courseproject.model.Items;
import com.example.courseproject.model.Rates;
import com.example.courseproject.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@Controller
public class RateController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RateRepository rateRepository;
    @Autowired
    private ItemRepository itemRepository;

    @PostMapping("/add_rate")
    public void addRate(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            if (!(userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())) {
                authentication.setAuthenticated(false);
                response.getWriter().write("login");
                return;
            }
            Integer userRate = request.getParameter("rate") != null ? Integer.parseInt(request.getParameter("rate")) : 0;
            Long itemId = request.getParameter("item_id") != null ? Long.parseLong(request.getParameter("item_id")) : 0;
            User user = userRepository.findByEmail(customUserDetails.getUsername());
            Items item = itemRepository.getOne(itemId);
            Rates rate = new Rates();
            rate = rateRepository.findByItemUser(user.getId(),item.getId());
            if(userRate >0){
                Date date = new Date();
                if(rate == null){
                    rate = new Rates();
                    rate.setCreateDate(date);
                } else {
                    rate.setUpdateDate(date);
                }
                rate.setRate(userRate);
                rate.setUser(user);
                rate.setItem(item);
                rateRepository.save(rate);
            }
            response.getWriter().write("success");
        } else {
            response.getWriter().write("login");
        }
    }

}
