package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("borrow_record")
public class BorrowRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer bookId;
    private String bookTitle;
    private Integer studentId;
    private String studentName;
    private String studentNo;
    private LocalDateTime borrowTime;
    private LocalDate dueDate;
    private LocalDateTime returnTime;
    private String status;
    private Integer isOverdue;
    private Integer renewCount;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
