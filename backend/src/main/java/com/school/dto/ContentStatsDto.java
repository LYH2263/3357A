package com.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentStatsDto {
    private Long courseCount;
    private Long courseWithFileCount;
    private Double courseFileCoverage;
    private Long experimentCount;
    private Long experimentWithFileCount;
    private Double experimentFileCoverage;
}
