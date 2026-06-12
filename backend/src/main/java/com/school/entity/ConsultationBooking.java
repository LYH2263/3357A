package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("consultation_booking")
public class ConsultationBooking {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer slotId;
    private Integer studentId;
    private String studentName;
    private String studentNo;
    private String question;
    private String status;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private LocalDateTime completeTime;
    private String teacherRemark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
