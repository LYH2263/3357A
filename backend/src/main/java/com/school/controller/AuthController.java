package com.school.controller;

import com.school.entity.User;
import com.school.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        return authService.login(
                loginData.get("username"),
                loginData.get("password"),
                loginData.get("role")
        );
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        return authService.registerStudent(user);
    }
}
