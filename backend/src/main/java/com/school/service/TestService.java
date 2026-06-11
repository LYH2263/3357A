package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.Test;
import com.school.mapper.TestMapper;
import org.springframework.stereotype.Service;

@Service
public class TestService extends ServiceImpl<TestMapper, Test> {
}
