package com.example.demo.authcontroller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    UserService userService;

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        boolean isValid = userService.loginUser(request); // Fixed: removed static call
        if (isValid) {
            return "Login Successful";
        } else {
            return "Invalid Credentials";
        }
    }
}