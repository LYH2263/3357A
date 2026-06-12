package com.school.dto;

import lombok.Data;

@Data
public class DirectoryQuery {
    private String type;
    private String keyword;
    private String expertise;
    private String teacherNo;
    private Integer classId;
    private String className;
    private String studentNo;
    private String status;
    private String nameInitial;
    private Integer pageNum = 1;
    private Integer pageSize = 12;
    private String groupBy;
    private String sortBy;
    private String sortOrder = "asc";
}
