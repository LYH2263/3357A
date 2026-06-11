package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("score")
public class Score {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer courseId;
    private Integer classId;
    private Integer studentId;
    private String studentName;
    private String studentNo;
    private BigDecimal regularScore;
    private BigDecimal midtermScore;
    private BigDecimal finalScore;
    private BigDecimal totalScore;
    private String grade;
    private Integer classRank;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
