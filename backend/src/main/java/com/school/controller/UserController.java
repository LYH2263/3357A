package com.school.controller;

import com.school.entity.User;
import com.school.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {
    @Autowired private UserService userService;

    @GetMapping("/list") public List<User> list() { return userService.list(); }
    @PostMapping("/save") public boolean save(@RequestBody User user) { return userService.saveOrUpdate(user); }
    @DeleteMapping("/delete/{id}") public boolean delete(@PathVariable Integer id) { return userService.removeById(id); }
    
    @PostMapping("/approve/{id}")
    public boolean approve(@PathVariable Integer id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setCheckedok("已通过");
            return userService.updateById(user);
        }
        return false;
    }

    @PostMapping("/reject/{id}")
    public boolean reject(@PathVariable Integer id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setCheckedok("已拒绝");
            return userService.updateById(user);
        }
        return false;
    }
}
