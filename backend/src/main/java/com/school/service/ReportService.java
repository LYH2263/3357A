package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.dto.*;
import com.school.entity.SavedReport;
import com.school.mapper.ReportMapper;
import com.school.mapper.SavedReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService extends ServiceImpl<SavedReportMapper, SavedReport> {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private SavedReportMapper savedReportMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAX_DAILY_POINTS = 60;

    public ReportDashboardDto getDashboard(ReportFilter filter) {
        if (filter == null) filter = new ReportFilter();
        normalizeFilter(filter);

        String tzOffset = resolveTimezoneOffset(filter.getTimezone());
        LocalDateTime startTime = toStartOfDay(filter.getStartDate());
        LocalDateTime endTime = toEndOfDay(filter.getEndDate());

        String samplingNote = detectSampling(filter.getStartDate(), filter.getEndDate());
        if (samplingNote != null) {
            long days = Duration.between(filter.getStartDate().atStartOfDay(), filter.getEndDate().atStartOfDay()).toDays();
            if (days > MAX_DAILY_POINTS) {
                LocalDate adjustedStart = filter.getEndDate().minusDays(MAX_DAILY_POINTS - 1);
                filter.setStartDate(adjustedStart);
                startTime = toStartOfDay(adjustedStart);
            }
        }

        ReportDashboardDto dto = new ReportDashboardDto();
        dto.setGeneratedAt(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        dto.setSamplingNote(samplingNote);
        dto.setAppliedFilter(filter);
        dto.setStudentStats(aggregateStudentStats(filter.getClassIds()));
        dto.setInteractionStats(aggregateInteractionStats(startTime, endTime, tzOffset));
        dto.setContentStats(aggregateContentStats());
        dto.setNewsStats(aggregateNewsStats(startTime, endTime, tzOffset));
        return dto;
    }

    private StudentStatsDto aggregateStudentStats(List<Integer> classIds) {
        List<Map<String, Object>> auditRows = reportMapper.countStudentByAuditStatus(classIds);
        List<Map<String, Object>> classRows = reportMapper.countStudentByClass(classIds);

        long total = 0, excellent = 0, approved = 0, pending = 0, rejected = 0;
        List<StudentStatsDto.NameValue> auditDist = new ArrayList<>();
        for (Map<String, Object> row : auditRows) {
            String status = String.valueOf(row.get("name"));
            Long cnt = ((Number) row.get("value")).longValue();
            auditDist.add(new StudentStatsDto.NameValue(status, cnt));
            total += cnt;
            if ("已通过".equals(status)) approved = cnt;
            else if ("待审核".equals(status)) pending = cnt;
            else rejected = cnt;
        }

        List<StudentStatsDto.ClassStudentStats> classStats = new ArrayList<>();
        for (Map<String, Object> row : classRows) {
            Integer cid = ((Number) row.get("classId")).intValue();
            String cname = String.valueOf(row.get("className"));
            Long sc = ((Number) row.get("studentCount")).longValue();
            Long ec = ((Number) row.get("excellentCount")).longValue();
            excellent += ec;
            double rate = sc > 0 ? Math.round(ec * 10000.0 / sc) / 100.0 : 0.0;
            classStats.add(new StudentStatsDto.ClassStudentStats(cid, cname, sc, ec, rate));
        }

        return new StudentStatsDto(total, excellent, approved, pending, rejected, auditDist, classStats);
    }

    private InteractionStatsDto aggregateInteractionStats(LocalDateTime startTime, LocalDateTime endTime, String tzOffset) {
        Map<String, Object> summary = reportMapper.countInteractionSummary(startTime, endTime);
        Long total = summary != null && summary.get("totalQuestions") != null
                ? ((Number) summary.get("totalQuestions")).longValue() : 0L;
        Long replied = summary != null && summary.get("repliedCount") != null
                ? ((Number) summary.get("repliedCount")).longValue() : 0L;
        Long unanswered = total - replied;
        Double replyRate = total > 0 ? Math.round(replied * 10000.0 / total) / 100.0 : 0.0;

        Double avgMin = reportMapper.getAvgResponseMinutes(startTime, endTime);
        Double roundedAvg = avgMin != null ? Math.round(avgMin * 100.0) / 100.0 : 0.0;

        List<Map<String, Object>> daily = reportMapper.countInteractionDaily(startTime, endTime, tzOffset);
        List<InteractionStatsDto.DailyInteraction> trend = daily.stream().map(row -> {
            String d = row.get("date") != null ? row.get("date").toString() : "";
            Long qc = row.get("questionCount") != null ? ((Number) row.get("questionCount")).longValue() : 0L;
            Long rc = row.get("replyCount") != null ? ((Number) row.get("replyCount")).longValue() : 0L;
            return new InteractionStatsDto.DailyInteraction(d, qc, rc);
        }).collect(Collectors.toList());

        return new InteractionStatsDto(total, replied, unanswered, replyRate, roundedAvg, trend);
    }

    private ContentStatsDto aggregateContentStats() {
        Map<String, Object> c = reportMapper.countCourseStats();
        Map<String, Object> e = reportMapper.countExperimentStats();

        Long cc = c != null && c.get("courseCount") != null ? ((Number) c.get("courseCount")).longValue() : 0L;
        Long cw = c != null && c.get("courseWithFileCount") != null ? ((Number) c.get("courseWithFileCount")).longValue() : 0L;
        Long ec = e != null && e.get("experimentCount") != null ? ((Number) e.get("experimentCount")).longValue() : 0L;
        Long ew = e != null && e.get("experimentWithFileCount") != null ? ((Number) e.get("experimentWithFileCount")).longValue() : 0L;

        Double cCov = cc > 0 ? Math.round(cw * 10000.0 / cc) / 100.0 : 0.0;
        Double eCov = ec > 0 ? Math.round(ew * 10000.0 / ec) / 100.0 : 0.0;

        return new ContentStatsDto(cc, cw, cCov, ec, ew, eCov);
    }

    private NewsStatsDto aggregateNewsStats(LocalDateTime startTime, LocalDateTime endTime, String tzOffset) {
        Long total = reportMapper.countNewsTotal(startTime, endTime);
        if (total == null) total = 0L;

        List<Map<String, Object>> daily = reportMapper.countNewsDaily(startTime, endTime, tzOffset);
        List<NewsStatsDto.DailyNews> trend = daily.stream().map(row -> {
            String d = row.get("date") != null ? row.get("date").toString() : "";
            Long cnt = row.get("count") != null ? ((Number) row.get("count")).longValue() : 0L;
            return new NewsStatsDto.DailyNews(d, cnt);
        }).collect(Collectors.toList());

        return new NewsStatsDto(total, trend);
    }

    private void normalizeFilter(ReportFilter filter) {
        if (filter.getEndDate() == null) filter.setEndDate(LocalDate.now());
        if (filter.getStartDate() == null) filter.setStartDate(filter.getEndDate().minusDays(29));
        if (filter.getStartDate().isAfter(filter.getEndDate())) {
            LocalDate tmp = filter.getStartDate();
            filter.setStartDate(filter.getEndDate());
            filter.setEndDate(tmp);
        }
    }

    private String detectSampling(LocalDate start, LocalDate end) {
        long days = Duration.between(start.atStartOfDay(), end.atStartOfDay()).toDays();
        if (days > MAX_DAILY_POINTS) {
            return String.format("时间范围超过%d天，已自动截断至最近%d天以保证性能。如需完整历史数据请分批导出。", MAX_DAILY_POINTS, MAX_DAILY_POINTS);
        }
        return null;
    }

    private LocalDateTime toStartOfDay(LocalDate d) {
        return d != null ? d.atStartOfDay() : null;
    }

    private LocalDateTime toEndOfDay(LocalDate d) {
        return d != null ? d.atTime(LocalTime.MAX) : null;
    }

    private String resolveTimezoneOffset(String tz) {
        try {
            if (tz == null || tz.isEmpty()) tz = "Asia/Shanghai";
            ZoneId zoneId = ZoneId.of(tz);
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            int offsetSec = now.getOffset().getTotalSeconds();
            int hours = offsetSec / 3600;
            int minutes = Math.abs((offsetSec % 3600) / 60);
            String sign = hours >= 0 ? "+" : "-";
            return String.format("%s%02d:%02d", sign, Math.abs(hours), minutes);
        } catch (Exception e) {
            return "+08:00";
        }
    }

    public SavedReport saveReportConfig(Integer teacherId, String reportName, ReportFilter filter) {
        SavedReport r = new SavedReport();
        r.setReportName(reportName);
        r.setTeacherId(teacherId);
        try {
            r.setFilterJson(objectMapper.writeValueAsString(filter));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("筛选配置序列化失败");
        }
        r.setCreateTime(LocalDateTime.now());
        r.setUpdateTime(LocalDateTime.now());
        savedReportMapper.insert(r);
        return r;
    }

    public List<SavedReport> listMyReports(Integer teacherId) {
        QueryWrapper<SavedReport> qw = new QueryWrapper<>();
        qw.eq("teacher_id", teacherId).orderByDesc("update_time");
        return savedReportMapper.selectList(qw);
    }

    public ReportFilter loadReportConfig(Integer reportId, Integer teacherId) {
        SavedReport r = savedReportMapper.selectById(reportId);
        if (r == null || !r.getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("报表不存在或无权限访问");
        }
        try {
            return objectMapper.readValue(r.getFilterJson(), ReportFilter.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("筛选配置解析失败");
        }
    }

    public boolean deleteReport(Integer reportId, Integer teacherId) {
        SavedReport r = savedReportMapper.selectById(reportId);
        if (r == null || !r.getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("报表不存在或无权限访问");
        }
        return savedReportMapper.deleteById(reportId) > 0;
    }
}
