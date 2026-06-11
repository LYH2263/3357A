package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evaluation_activity")
public class EvaluationActivity {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Integer creatorId;
    private String creatorType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
