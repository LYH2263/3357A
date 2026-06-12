package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("discipline_record")
public class DisciplineRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer studentId;
    private String studentName;
    private String studentNo;
    private Integer classId;
    private String className;
    private String category;
    private String recordType;
    private String reason;
    private LocalDate occurDate;
    private String severity;
    private String evidenceFile;
    private String evidenceFileName;
    private String status;
    private String revokeReason;
    private LocalDateTime revokeTime;
    private Integer revokeTeacherId;
    private String revokeTeacherName;
    private Integer creatorId;
    private String creatorName;
    private String batchId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
