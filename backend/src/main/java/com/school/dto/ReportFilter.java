package com.school.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ReportFilter {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Integer> classIds;
    private String timezone = "Asia/Shanghai";
}
