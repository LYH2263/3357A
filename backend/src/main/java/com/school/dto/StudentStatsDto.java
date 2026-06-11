package com.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentStatsDto {
    private Long totalCount;
    private Long excellentCount;
    private Long approvedCount;
    private Long pendingCount;
    private Long rejectedCount;
    private List<NameValue> auditStatusDistribution;
    private List<ClassStudentStats> classStudentList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NameValue {
        private String name;
        private Long value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassStudentStats {
        private Integer classId;
        private String className;
        private Long studentCount;
        private Long excellentCount;
        private Double excellentRate;
    }
}
