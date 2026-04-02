package com.example.demo.controller;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.usermodel.User;
import com.example.demo.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserService service;

    @PostMapping
    public String createUser(@RequestBody CreateUserRequest request) throws Exception{
        return service.createUser(request);
    }

    @GetMapping
    public List<User> getAllUsers() throws Exception{
        return service.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable String id) throws Exception{
        return service.getUser(id);
    }

    @PutMapping("/{id}")
    public String updateUser(@PathVariable String id,@RequestBody User user){
        return service.updateUser(id,user);
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable String id) throws Exception{
        return service.deleteUser(id);
    }

}