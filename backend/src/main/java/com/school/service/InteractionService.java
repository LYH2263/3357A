package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.Interaction;
import com.school.mapper.InteractionMapper;
import org.springframework.stereotype.Service;

@Service
public class InteractionService extends ServiceImpl<InteractionMapper, Interaction> {
}
