package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@TableName("consultation_slot")
public class ConsultationSlot {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer teacherId;
    private String teacherName;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String locationType;
    private Integer capacity;
    private Integer bookedCount;
    @Version
    private Integer version;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
