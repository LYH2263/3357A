package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.Classes;
import com.school.mapper.ClassesMapper;
import org.springframework.stereotype.Service;

@Service
public class ClassesService extends ServiceImpl<ClassesMapper, Classes> {
}
