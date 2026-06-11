package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.Teacher;
import com.school.mapper.TeacherMapper;
import org.springframework.stereotype.Service;

@Service
public class TeacherService extends ServiceImpl<TeacherMapper, Teacher> {
}
