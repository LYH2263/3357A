package com.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InteractionStatsDto {
    private Long totalQuestions;
    private Long repliedCount;
    private Long unansweredCount;
    private Double replyRate;
    private Double avgResponseMinutes;
    private List<DailyInteraction> dailyTrend;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyInteraction {
        private String date;
        private Long questionCount;
        private Long replyCount;
    }
}
