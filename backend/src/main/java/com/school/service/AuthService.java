package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.Teacher;
import com.school.entity.User;
import com.school.mapper.TeacherMapper;
import com.school.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    public Map<String, Object> login(String username, String password, String role) {
        Map<String, Object> result = new HashMap<>();
        if ("student".equals(role)) {
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, username)
                    .eq(User::getUserpassword, password));
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户名或密码错误");
            } else if (!"已通过".equals(user.getCheckedok())) {
                result.put("success", false);
                result.put("message", "账号审核中，请联系老师批准");
            } else {
                result.put("success", true);
                result.put("user", user);
                result.put("role", "student");
            }
        } else if ("teacher".equals(role)) {
            Teacher teacher = teacherMapper.selectOne(new LambdaQueryWrapper<Teacher>()
                    .eq(Teacher::getTname, username)
                    .eq(Teacher::getTpassword, password));
            if (teacher == null) {
                result.put("success", false);
                result.put("message", "用户名或密码错误");
            } else {
                result.put("success", true);
                result.put("user", teacher);
                result.put("role", "teacher");
            }
        } else {
            result.put("success", false);
            result.put("message", "非法角色");
        }
        return result;
    }

    public Map<String, Object> registerStudent(User user) {
        Map<String, Object> result = new HashMap<>();
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername())
                .or()
                .eq(User::getUserno, user.getUserno()));
        if (existing != null) {
            result.put("success", false);
            result.put("message", "用户名或学号已存在");
            return result;
        }
        user.setCheckedok("待审核");
        userMapper.insert(user);
        result.put("success", true);
        result.put("message", "注册成功，请等待老师审核");
        return result;
    }
}
