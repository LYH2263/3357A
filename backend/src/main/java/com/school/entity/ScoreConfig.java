package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("score_config")
public class ScoreConfig {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer courseId;
    private Integer classId;
    private BigDecimal regularWeight;
    private BigDecimal midtermWeight;
    private BigDecimal finalWeight;
    private Integer scorePrecision;
    private BigDecimal gradeExcellent;
    private BigDecimal gradeGood;
    private BigDecimal gradeMedium;
    private BigDecimal gradePass;
    private Integer isLocked;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
