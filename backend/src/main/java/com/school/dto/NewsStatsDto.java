package com.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsStatsDto {
    private Long totalCount;
    private List<DailyNews> dailyTrend;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyNews {
        private String date;
        private Long count;
    }
}
