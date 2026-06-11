package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evaluation_score")
public class EvaluationScore {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer activityId;
    private Integer teacherId;
    private Integer dimensionId;
    private Integer score;
    private String anonymousToken;
    private LocalDateTime createTime;
}
