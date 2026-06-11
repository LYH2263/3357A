package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evaluation_submit")
public class EvaluationSubmit {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer activityId;
    private Integer studentId;
    private Integer teacherId;
    private String anonymousToken;
    private LocalDateTime submitTime;
}
