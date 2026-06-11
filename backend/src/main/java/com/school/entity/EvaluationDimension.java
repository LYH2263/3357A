package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("evaluation_dimension")
public class EvaluationDimension {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer activityId;
    private String name;
    private BigDecimal weight;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
