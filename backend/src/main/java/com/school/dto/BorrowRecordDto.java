package com.school.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BorrowRecordDto {
    private Integer id;
    private Integer bookId;
    private String bookTitle;
    private String bookCover;
    private Integer studentId;
    private String studentName;
    private String studentNo;
    private String className;
    private LocalDateTime borrowTime;
    private LocalDate dueDate;
    private LocalDateTime returnTime;
    private String status;
    private Integer isOverdue;
    private Integer renewCount;
    private String remark;
    private LocalDateTime createTime;
    private String author;
    private String isbn;
    private String category;
}
