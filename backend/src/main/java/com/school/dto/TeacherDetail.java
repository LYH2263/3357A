package com.school.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TeacherDetail {
    private Integer tid;
    private String tname;
    private String tno;
    private String tpic;
    private String tdescript;
    private LocalDate tdate;
    private String expertise;
    private String nameInitial;
    private String pinyin;
}
