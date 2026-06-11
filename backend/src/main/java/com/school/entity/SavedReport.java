package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("saved_report")
public class SavedReport {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String reportName;
    private Integer teacherId;
    private String filterJson;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
