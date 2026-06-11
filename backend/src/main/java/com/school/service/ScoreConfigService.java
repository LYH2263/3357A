package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.ScoreConfig;
import com.school.mapper.ScoreConfigMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ScoreConfigService extends ServiceImpl<ScoreConfigMapper, ScoreConfig> {

    public ScoreConfig getConfig(Integer courseId, Integer classId) {
        ScoreConfig config = this.getOne(new LambdaQueryWrapper<ScoreConfig>()
                .eq(ScoreConfig::getCourseId, courseId)
                .eq(ScoreConfig::getClassId, classId));
        if (config == null) {
            config = createDefaultConfig(courseId, classId);
        }
        return config;
    }

    public ScoreConfig createDefaultConfig(Integer courseId, Integer classId) {
        ScoreConfig config = new ScoreConfig();
        config.setCourseId(courseId);
        config.setClassId(classId);
        config.setRegularWeight(new BigDecimal("30.00"));
        config.setMidtermWeight(new BigDecimal("30.00"));
        config.setFinalWeight(new BigDecimal("40.00"));
        config.setScorePrecision(1);
        config.setGradeExcellent(new BigDecimal("90.00"));
        config.setGradeGood(new BigDecimal("80.00"));
        config.setGradeMedium(new BigDecimal("70.00"));
        config.setGradePass(new BigDecimal("60.00"));
        config.setIsLocked(0);
        this.save(config);
        return config;
    }

    public boolean saveConfig(ScoreConfig config) {
        BigDecimal sum = config.getRegularWeight()
                .add(config.getMidtermWeight())
                .add(config.getFinalWeight());
        if (sum.compareTo(new BigDecimal("100.00")) != 0) {
            throw new IllegalArgumentException("权重之和必须等于100%");
        }
        if (config.getId() == null) {
            return this.save(config);
        } else {
            return this.updateById(config);
        }
    }

    public boolean toggleLock(Integer id, boolean locked) {
        ScoreConfig config = new ScoreConfig();
        config.setId(id);
        config.setIsLocked(locked ? 1 : 0);
        return this.updateById(config);
    }

    public boolean isLocked(Integer courseId, Integer classId) {
        ScoreConfig config = this.getOne(new LambdaQueryWrapper<ScoreConfig>()
                .eq(ScoreConfig::getCourseId, courseId)
                .eq(ScoreConfig::getClassId, classId));
        return config != null && config.getIsLocked() == 1;
    }
}
