package com.school.controller;

import com.school.dto.ReportDashboardDto;
import com.school.dto.ReportFilter;
import com.school.entity.SavedReport;
import com.school.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
@CrossOrigin
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping("/dashboard")
    public ReportDashboardDto dashboard(@RequestBody(required = false) ReportFilter filter) {
        return reportService.getDashboard(filter);
    }

    @GetMapping("/my-reports")
    public List<SavedReport> myReports(@RequestParam Integer teacherId) {
        return reportService.listMyReports(teacherId);
    }

    @PostMapping("/save")
    public Map<String, Object> save(@RequestBody Map<String, Object> body) {
        Integer teacherId = null;
        Object tidObj = body.get("teacherId");
        if (tidObj instanceof Number) {
            teacherId = ((Number) tidObj).intValue();
        } else if (tidObj != null) {
            try { teacherId = Integer.parseInt(tidObj.toString()); } catch (NumberFormatException ignored) {}
        }
        String reportName = body.get("reportName") != null ? body.get("reportName").toString() : null;
        ReportFilter filter = null;
        if (body.get("filter") instanceof Map) {
            filter = new ReportFilter();
            Map<String, Object> fm = (Map<String, Object>) body.get("filter");
            if (fm.get("startDate") != null) filter.setStartDate(java.time.LocalDate.parse(fm.get("startDate").toString()));
            if (fm.get("endDate") != null) filter.setEndDate(java.time.LocalDate.parse(fm.get("endDate").toString()));
            if (fm.get("classIds") instanceof List) {
                List<Integer> cids = ((List<?>) fm.get("classIds")).stream()
                        .map(o -> {
                            if (o instanceof Number) return ((Number) o).intValue();
                            try { return Integer.parseInt(o.toString()); } catch (Exception e) { return null; }
                        })
                        .filter(java.util.Objects::nonNull)
                        .collect(java.util.stream.Collectors.toList());
                filter.setClassIds(cids);
            }
            if (fm.get("timezone") != null) filter.setTimezone(fm.get("timezone").toString());
        }
        if (teacherId == null || reportName == null || reportName.trim().isEmpty()) {
            throw new IllegalArgumentException("教师ID和报表名称必填");
        }
        SavedReport r = reportService.saveReportConfig(teacherId, reportName.trim(), filter);
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("id", r.getId());
        res.put("message", "保存成功");
        return res;
    }

    @GetMapping("/load/{id}")
    public ReportFilter load(@PathVariable Integer id, @RequestParam Integer teacherId) {
        return reportService.loadReportConfig(id, teacherId);
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> delete(@PathVariable Integer id, @RequestParam Integer teacherId) {
        boolean ok = reportService.deleteReport(id, teacherId);
        Map<String, Object> res = new HashMap<>();
        res.put("success", ok);
        res.put("message", ok ? "删除成功" : "删除失败");
        return res;
    }
}
