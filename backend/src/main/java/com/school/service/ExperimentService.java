package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.Experiment;
import com.school.mapper.ExperimentMapper;
import org.springframework.stereotype.Service;

@Service
public class ExperimentService extends ServiceImpl<ExperimentMapper, Experiment> {
}
