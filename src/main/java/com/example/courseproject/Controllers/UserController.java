package com.example.courseproject.Controllers;

import com.example.courseproject.Services.CustomUserDetails;
import com.example.courseproject.Repositories.RoleRepository;
import com.example.courseproject.Repositories.UserRepository;
import com.example.courseproject.Services.FileService;
import com.example.courseproject.model.Language;
import com.example.courseproject.model.Provider;
import com.example.courseproject.model.Role;
import com.example.courseproject.model.User;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private FileService fileService;

    @GetMapping("/user_profil")
    public String viewUserProfil(Authentication authentication, HttpServletRequest request, HttpServletResponse response){
        if(authentication != null && authentication.isAuthenticated()){
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            if(!(userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())){
                authentication.setAuthenticated(false);
                return "login";
            }
            User user = userRepository.getUserByEmail(customUserDetails.getUsername());
            request.setAttribute("username",customUserDetails.getFullName());
            request.setAttribute("user",user);
            request.setAttribute("lang",user.getLanguage().toString());
            request.setAttribute("role",customUserDetails.getRole().getName());
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
            localeResolver.setLocale(request, response,new Locale((user != null && user.getLanguage() != null) ? user.getLanguage().toString() : String.valueOf(Language.en)));

            return "user_profil";
        }
        return "index";
    }

    @GetMapping("/register")
    public String showSignUpForm(Model model){
        model.addAttribute("user", new User());
        return "signup_form";
    }

    @GetMapping("/list_users")
    public String viewUsersList(Authentication authentication, Model model, HttpServletRequest request, HttpServletResponse response){
        if(authentication != null && authentication.isAuthenticated()){
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            if(!(userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())){
                authentication.setAuthenticated(false);
                return "login";
            }
            List<User> userList = userRepository.findAll();
            User user = userRepository.getUserByEmail(customUserDetails.getUsername());
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
            localeResolver.setLocale(request, response,new Locale((user != null && user.getLanguage() != null) ? user.getLanguage().toString() : String.valueOf(Language.en)));
            model.addAttribute("listUser",userList);
            return "users";
        } else {
            return "login";
        }
    }

    @PostMapping("/block")
    public void blockUser(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(authentication != null && authentication.isAuthenticated()){
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            if(!(userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())){
                authentication.setAuthenticated(false);
                response.setContentType("text/html");
                response.getWriter().write("login");
                return;
            }
            String block_ids = request.getParameter("id");
            boolean flag = Boolean.parseBoolean(request.getParameter("flag"));
            boolean isUser=false;
            for (String id:block_ids.split(",")){
                if(customUserDetails.getUserId() == Long.valueOf(id)){
                    isUser = true;
                }
                userRepository.updateStatusById(Long.valueOf(id), flag);
            }
            if(isUser && !flag){
                userRepository.updateStatusById(customUserDetails.getUserId(),flag);
                response.setContentType("text/html");
                response.getWriter().write("login");
                return;
            }
            List<User> userList = userRepository.findAll();
            Gson gson = new Gson();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(gson.toJson(userList));
        } else {
            response.setContentType("text/html");
            response.getWriter().write("error");
        }
    }

    @PostMapping("/role")
    public void setUserRole(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(authentication != null && authentication.isAuthenticated()){
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            String user_ids = request.getParameter("id");
            Integer roleId = Integer.parseInt(request.getParameter("roleId"));
            Role role = roleRepository.findRoleById(roleId);
            boolean isUser=false;
            for (String id:user_ids.split(",")){
                if(customUserDetails.getUserId() == Long.valueOf(id)){
                    isUser = true;
                }
                userRepository.updateRoleById(Long.valueOf(id), role);
            }
            if(isUser && role.getName().equals("USER")){
                userRepository.updateRoleById(customUserDetails.getUserId(), role);
                response.setContentType("text/html");
                response.getWriter().write("login");
                return;
            }
            List<User> userList = userRepository.findAll();
            Gson gson = new Gson();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(gson.toJson(userList));
        }
    }

    @PostMapping("/delete")
    public void deleteUser(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if(authentication != null && authentication.isAuthenticated()){
            String delete_ids = request.getParameter("id");
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            if(!(userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())){
                authentication.setAuthenticated(false);
                response.setContentType("text/html");
                response.getWriter().write("login");
                return;
            }
            for (String id:delete_ids.split(",")){
                userRepository.deleteById(Long.valueOf(id));
                if(customUserDetails.getUserId() == Long.valueOf(id)){
                    authentication.setAuthenticated(false);
                }
            }
            if(!authentication.isAuthenticated()){
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("login");
            } else {
                List<User> userList = userRepository.findAll();
                Gson gson = new Gson();
                response.getWriter().write(gson.toJson(userList));
            }
        } else {
            response.setContentType("text/html");
            response.getWriter().write("login");
        }
    }

    @PostMapping("/update_user")
    public void updateUser(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        if(authentication != null && authentication.isAuthenticated()){
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            if(!(userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())){
                authentication.setAuthenticated(false);
                response.getWriter().write("login");
                return;
            }
            String user_name = request.getParameter("user_name");
            String surname = request.getParameter("surname");
            String phone_number = request.getParameter("phone_number");
            String address = request.getParameter("address");
            String email = request.getParameter("email");
            User user = userRepository.findByEmail(customUserDetails.getUsername());
            user.setFirstName(user_name);
            user.setLastName(surname);
            user.setPhoneNumber(phone_number);
            user.setAddress(address);
            user.setEmail(email);
            Date date = new Date();
            user.setUpdateDate(date);
            userRepository.save(user);
            response.getWriter().write("success");
        } else {
            response.getWriter().write("login");
        }
    }
    @PostMapping("/set_lang")
    public void setLang(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        if(authentication != null && authentication.isAuthenticated()){
                CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            if(!(userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())){
                authentication.setAuthenticated(false);
                response.getWriter().write("login");
                return;
            }
            String lang = request.getParameter("lang");
            User user = userRepository.findByEmail(customUserDetails.getUsername());
            user.setLanguage(Language.valueOf(lang));
            Date date = new Date();
            user.setUpdateDate(date);
            userRepository.save(user);

            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
            localeResolver.setLocale(request, response,new Locale(lang!=null?lang: String.valueOf(Language.en)));
            response.getWriter().write("success");
        } else {
            response.getWriter().write("login");
        }
    }

    @PostMapping("/update_pass")
    public void updateUserPass(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        if(authentication != null && authentication.isAuthenticated()){
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            if(!(userRepository.findByEmail(customUserDetails.getUsername()) != null && userRepository.findByEmail(customUserDetails.getUsername()).isEnabled())){
                authentication.setAuthenticated(false);
                response.getWriter().write("login");
                return;
            }
            String old_pass = request.getParameter("old_pass");
            String pass1 = request.getParameter("pass1") != null ? request.getParameter("pass1") : "";
            String pass2 = request.getParameter("pass2") != null ? request.getParameter("pass2") : "";
            User user = userRepository.findByEmail(customUserDetails.getUsername());
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if(encoder.matches(old_pass,user.getPassword()) && pass1.equals(pass2) && pass1.length()>0){
                user.setPassword(encoder.encode(pass1));
                Date date = new Date();
                user.setUpdateDate(date);
                userRepository.save(user);
                response.getWriter().write("success");
            } else {
                response.getWriter().write("error");
            }
            return;
        } else {
            response.getWriter().write("login");
        }
    }

    @PostMapping("/process_register")
    public String processRegistration(User user, Model model, HttpServletRequest request) throws IOException {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if(userRepository.findByEmail(user.getEmail()) != null){
            model.addAttribute("errorMsg1", "Email already exists !!!");
            return "signup_form";
        }
        user.setPassword(encoder.encode(user.getPassword()));
        user.setEnabled(true);
        Date date = new Date();
        user.setRegDate(date);
        user.setLastLoginDate(date);
        Role role = new Role();
        role = roleRepository.findByRoleName("USER");
        if(role == null){
            role = new Role();
            role.setName("USER");
            role = roleRepository.save(role);
        }
        user.setRole(role);
        user.setProvider(Provider.LOCAL);
        user.setLanguage(Language.en);
        user.setImageUrl(fileService.getFilePath(null));
        userRepository.save(user);
        return "register_success";
    }
}
