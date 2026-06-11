package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evaluation_activity_class")
public class EvaluationActivityClass {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer activityId;
    private Integer classId;
    private LocalDateTime createTime;
}
