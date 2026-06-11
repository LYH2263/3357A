package com.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDashboardDto {
    private String generatedAt;
    private String samplingNote;
    private ReportFilter appliedFilter;
    private StudentStatsDto studentStats;
    private InteractionStatsDto interactionStats;
    private ContentStatsDto contentStats;
    private NewsStatsDto newsStats;
}
