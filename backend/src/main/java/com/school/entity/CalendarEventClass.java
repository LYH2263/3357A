package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("calendar_event_class")
public class CalendarEventClass {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer eventId;
    private Integer classId;
    private LocalDateTime createTime;
}
