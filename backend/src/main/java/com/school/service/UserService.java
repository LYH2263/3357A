package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.User;
import com.school.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
}
