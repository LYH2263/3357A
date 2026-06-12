package com.school.controller;

import com.school.entity.DisciplineRecord;
import com.school.service.DisciplineRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discipline")
@CrossOrigin
public class DisciplineRecordController {

    @Autowired
    private DisciplineRecordService disciplineRecordService;

    @PostMapping("/add")
    public DisciplineRecord addRecord(@RequestBody DisciplineRecord record) {
        return disciplineRecordService.addRecord(record);
    }

    @PostMapping("/batch-add")
    public List<DisciplineRecord> batchAddRecords(@RequestBody Map<String, Object> params) {
        Integer classId = Integer.parseInt(params.get("classId").toString());
        String category = (String) params.get("category");
        String recordType = (String) params.get("recordType");
        String reason = (String) params.get("reason");
        String occurDate = (String) params.get("occurDate");
        String severity = (String) params.getOrDefault("severity", "medium");
        String evidenceFile = (String) params.get("evidenceFile");
        String evidenceFileName = (String) params.get("evidenceFileName");
        Integer creatorId = Integer.parseInt(params.get("creatorId").toString());
        String creatorName = (String) params.get("creatorName");

        return disciplineRecordService.batchAddRecords(classId, category, recordType, reason,
                occurDate, severity, evidenceFile, evidenceFileName, creatorId, creatorName);
    }

    @PostMapping("/revoke")
    public DisciplineRecord revokeRecord(@RequestBody Map<String, Object> params) {
        Integer recordId = Integer.parseInt(params.get("recordId").toString());
        String revokeReason = (String) params.get("revokeReason");
        Integer teacherId = Integer.parseInt(params.get("teacherId").toString());
        String teacherName = (String) params.get("teacherName");

        return disciplineRecordService.revokeRecord(recordId, revokeReason, teacherId, teacherName);
    }

    @GetMapping("/student")
    public List<DisciplineRecord> getStudentRecords(@RequestParam Integer studentId) {
        return disciplineRecordService.getStudentRecords(studentId);
    }

    @GetMapping("/query")
    public List<DisciplineRecord> queryRecords(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String recordType,
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status) {
        return disciplineRecordService.queryRecords(category, recordType, classId, startDate, endDate, status);
    }

    @GetMapping("/statistics")
    public Map<String, Object> getStatistics(@RequestParam(required = false) Integer classId) {
        return disciplineRecordService.getStatistics(classId);
    }
}
