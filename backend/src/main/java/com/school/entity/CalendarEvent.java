package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("calendar_event")
public class CalendarEvent {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String eventType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String remark;
    private Integer creatorId;
    private String creatorName;
    private Integer isArchived;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
