package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.Classes;
import com.school.entity.DisciplineRecord;
import com.school.entity.User;
import com.school.mapper.DisciplineRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DisciplineRecordService extends ServiceImpl<DisciplineRecordMapper, DisciplineRecord> {

    @Autowired
    private UserService userService;

    @Autowired
    private ClassesService classesService;

    @Transactional
    public DisciplineRecord addRecord(DisciplineRecord record) {
        fillStudentInfo(record);
        record.setStatus("active");
        this.save(record);
        syncYouxiuok(record.getStudentId());
        return this.getById(record.getId());
    }

    @Transactional
    public List<DisciplineRecord> batchAddRecords(Integer classId, String category, String recordType,
                                                   String reason, String occurDate, String severity,
                                                   String evidenceFile, String evidenceFileName,
                                                   Integer creatorId, String creatorName) {
        List<User> students = userService.lambdaQuery()
                .eq(User::getClassId, classId)
                .list();

        if (students.isEmpty()) {
            throw new IllegalArgumentException("该班级没有学生");
        }

        String batchId = UUID.randomUUID().toString().replace("-", "");
        Classes classes = classesService.getById(classId);
        String className = classes != null ? classes.getCname() : "";

        List<DisciplineRecord> records = new ArrayList<>();
        for (User student : students) {
            DisciplineRecord record = new DisciplineRecord();
            record.setStudentId(student.getUid());
            record.setStudentName(student.getUsername());
            record.setStudentNo(student.getUserno());
            record.setClassId(classId);
            record.setClassName(className);
            record.setCategory(category);
            record.setRecordType(recordType);
            record.setReason(reason);
            record.setOccurDate(java.time.LocalDate.parse(occurDate));
            record.setSeverity(severity);
            record.setEvidenceFile(evidenceFile);
            record.setEvidenceFileName(evidenceFileName);
            record.setCreatorId(creatorId);
            record.setCreatorName(creatorName);
            record.setBatchId(batchId);
            record.setStatus("active");
            records.add(record);
        }

        this.saveBatch(records);

        for (User student : students) {
            syncYouxiuok(student.getUid());
        }

        return records;
    }

    @Transactional
    public DisciplineRecord revokeRecord(Integer recordId, String revokeReason, Integer teacherId, String teacherName) {
        DisciplineRecord record = this.getById(recordId);
        if (record == null) {
            throw new IllegalArgumentException("记录不存在");
        }
        if ("revoked".equals(record.getStatus())) {
            throw new IllegalStateException("该记录已被撤销，不可重复操作");
        }

        record.setStatus("revoked");
        record.setRevokeReason(revokeReason);
        record.setRevokeTime(LocalDateTime.now());
        record.setRevokeTeacherId(teacherId);
        record.setRevokeTeacherName(teacherName);
        this.updateById(record);

        syncYouxiuok(record.getStudentId());
        return this.getById(recordId);
    }

    public List<DisciplineRecord> getStudentRecords(Integer studentId) {
        return this.list(new LambdaQueryWrapper<DisciplineRecord>()
                .eq(DisciplineRecord::getStudentId, studentId)
                .orderByDesc(DisciplineRecord::getOccurDate)
                .orderByDesc(DisciplineRecord::getCreateTime));
    }

    public List<DisciplineRecord> queryRecords(String category, String recordType, Integer classId,
                                                String startDate, String endDate, String status) {
        LambdaQueryWrapper<DisciplineRecord> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            wrapper.eq(DisciplineRecord::getCategory, category);
        }
        if (recordType != null && !recordType.isEmpty()) {
            wrapper.eq(DisciplineRecord::getRecordType, recordType);
        }
        if (classId != null) {
            wrapper.eq(DisciplineRecord::getClassId, classId);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(DisciplineRecord::getOccurDate, java.time.LocalDate.parse(startDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(DisciplineRecord::getOccurDate, java.time.LocalDate.parse(endDate));
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(DisciplineRecord::getStatus, status);
        }
        wrapper.orderByDesc(DisciplineRecord::getOccurDate)
               .orderByDesc(DisciplineRecord::getCreateTime);
        return this.list(wrapper);
    }

    public Map<String, Object> getStatistics(Integer classId) {
        LambdaQueryWrapper<DisciplineRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DisciplineRecord::getStatus, "active");
        if (classId != null) {
            wrapper.eq(DisciplineRecord::getClassId, classId);
        }
        List<DisciplineRecord> records = this.list(wrapper);

        Map<String, Object> result = new HashMap<>();

        Map<String, Long> typeCount = records.stream()
                .collect(Collectors.groupingBy(DisciplineRecord::getRecordType, Collectors.counting()));
        result.put("typeCount", typeCount);

        Map<String, Long> categoryCount = records.stream()
                .collect(Collectors.groupingBy(DisciplineRecord::getCategory, Collectors.counting()));
        result.put("categoryCount", categoryCount);

        Map<String, Long> severityCount = records.stream()
                .collect(Collectors.groupingBy(DisciplineRecord::getSeverity, Collectors.counting()));
        result.put("severityCount", severityCount);

        Map<Integer, List<DisciplineRecord>> studentRecords = records.stream()
                .collect(Collectors.groupingBy(DisciplineRecord::getStudentId));
        List<Map<String, Object>> studentDistribution = new ArrayList<>();
        for (Map.Entry<Integer, List<DisciplineRecord>> entry : studentRecords.entrySet()) {
            List<DisciplineRecord> sr = entry.getValue();
            Map<String, Object> item = new HashMap<>();
            item.put("studentId", entry.getKey());
            item.put("studentName", sr.get(0).getStudentName());
            item.put("studentNo", sr.get(0).getStudentNo());
            item.put("className", sr.get(0).getClassName());
            item.put("totalCount", sr.size());
            item.put("rewardCount", sr.stream().filter(r -> "reward".equals(r.getCategory())).count());
            item.put("punishmentCount", sr.stream().filter(r -> "punishment".equals(r.getCategory())).count());
            studentDistribution.add(item);
        }
        studentDistribution.sort((a, b) -> Long.compare((Long) b.get("totalCount"), (Long) a.get("totalCount")));
        result.put("studentDistribution", studentDistribution);
        result.put("totalRecords", records.size());

        return result;
    }

    private void fillStudentInfo(DisciplineRecord record) {
        User student = userService.getById(record.getStudentId());
        if (student == null) {
            throw new IllegalArgumentException("学生不存在，ID: " + record.getStudentId());
        }
        record.setStudentName(student.getUsername());
        record.setStudentNo(student.getUserno());
        record.setClassId(student.getClassId());
        record.setClassName(student.getClassname());
    }

    private void syncYouxiuok(Integer studentId) {
        List<DisciplineRecord> activeRecords = this.list(new LambdaQueryWrapper<DisciplineRecord>()
                .eq(DisciplineRecord::getStudentId, studentId)
                .eq(DisciplineRecord::getStatus, "active"));

        boolean hasHighReward = activeRecords.stream()
                .anyMatch(r -> "reward".equals(r.getCategory()) && "high".equals(r.getSeverity()));

        boolean hasHighPunishment = activeRecords.stream()
                .anyMatch(r -> "punishment".equals(r.getCategory()) && "high".equals(r.getSeverity()));

        User student = userService.getById(studentId);
        if (student != null) {
            if (hasHighPunishment) {
                student.setYouxiuok("否");
            } else if (hasHighReward) {
                student.setYouxiuok("是");
            }
            userService.updateById(student);
        }
    }
}
