package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("teacher_class")
public class TeacherClass {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer teacherId;
    private String teacherName;
    private Integer classId;
    private String className;
    private String courseName;
    private LocalDateTime createTime;
}
