package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.*;
import com.school.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EvaluationActivityService extends ServiceImpl<EvaluationActivityMapper, EvaluationActivity> {

    @Autowired
    private EvaluationDimensionMapper dimensionMapper;

    @Autowired
    private EvaluationActivityClassMapper activityClassMapper;

    @Autowired
    private EvaluationActivityTeacherMapper activityTeacherMapper;

    @Autowired
    private TeacherService teacherService;

    public List<EvaluationActivity> listAll() {
        return this.list(new LambdaQueryWrapper<EvaluationActivity>()
                .orderByDesc(EvaluationActivity::getCreateTime));
    }

    public List<EvaluationActivity> listByStatus(String status) {
        return this.list(new LambdaQueryWrapper<EvaluationActivity>()
                .eq(EvaluationActivity::getStatus, status)
                .orderByDesc(EvaluationActivity::getCreateTime));
    }

    public List<EvaluationActivity> listByStudentClass(Integer classId) {
        List<EvaluationActivityClass> activityClasses = activityClassMapper.selectList(
                new LambdaQueryWrapper<EvaluationActivityClass>()
                        .eq(EvaluationActivityClass::getClassId, classId));

        if (activityClasses.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> activityIds = activityClasses.stream()
                .map(EvaluationActivityClass::getActivityId)
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        return this.list(new LambdaQueryWrapper<EvaluationActivity>()
                .in(EvaluationActivity::getId, activityIds)
                .orderByDesc(EvaluationActivity::getCreateTime));
    }

    public Map<String, Object> getDetail(Integer id) {
        EvaluationActivity activity = this.getById(id);
        if (activity == null) {
            throw new IllegalArgumentException("活动不存在");
        }

        List<EvaluationDimension> dimensions = dimensionMapper.selectList(
                new LambdaQueryWrapper<EvaluationDimension>()
                        .eq(EvaluationDimension::getActivityId, id)
                        .orderByAsc(EvaluationDimension::getSortOrder));

        List<EvaluationActivityClass> activityClasses = activityClassMapper.selectList(
                new LambdaQueryWrapper<EvaluationActivityClass>()
                        .eq(EvaluationActivityClass::getActivityId, id));

        List<Integer> classIds = activityClasses.stream()
                .map(EvaluationActivityClass::getClassId)
                .collect(Collectors.toList());

        List<EvaluationActivityTeacher> activityTeachers = activityTeacherMapper.selectList(
                new LambdaQueryWrapper<EvaluationActivityTeacher>()
                        .eq(EvaluationActivityTeacher::getActivityId, id));

        Map<String, Object> result = new HashMap<>();
        result.put("activity", activity);
        result.put("dimensions", dimensions);
        result.put("classIds", classIds);
        result.put("teachers", activityTeachers);
        return result;
    }

    @Transactional
    public EvaluationActivity createActivity(EvaluationActivity activity,
                                              List<EvaluationDimension> dimensions,
                                              List<Integer> classIds,
                                              List<Integer> teacherIds) {
        BigDecimal totalWeight = dimensions.stream()
                .map(EvaluationDimension::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalWeight.compareTo(new BigDecimal("100")) != 0) {
            throw new IllegalArgumentException("所有维度权重之和必须等于100%");
        }

        for (EvaluationDimension dim : dimensions) {
            if (dim.getWeight().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("维度权重必须大于0");
            }
            if (dim.getName() == null || dim.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("维度名称不能为空");
            }
        }

        if (classIds == null || classIds.isEmpty()) {
            throw new IllegalArgumentException("请至少选择一个参评班级");
        }
        if (teacherIds == null || teacherIds.isEmpty()) {
            throw new IllegalArgumentException("请至少选择一位参评教师");
        }

        activity.setStatus("ongoing");
        activity.setCreateTime(LocalDateTime.now());
        this.save(activity);

        for (int i = 0; i < dimensions.size(); i++) {
            EvaluationDimension dim = dimensions.get(i);
            dim.setActivityId(activity.getId());
            dim.setSortOrder(i + 1);
            dim.setCreateTime(LocalDateTime.now());
            dimensionMapper.insert(dim);
        }

        for (Integer classId : classIds) {
            EvaluationActivityClass ac = new EvaluationActivityClass();
            ac.setActivityId(activity.getId());
            ac.setClassId(classId);
            ac.setCreateTime(LocalDateTime.now());
            activityClassMapper.insert(ac);
        }

        for (Integer teacherId : teacherIds) {
            Teacher teacher = teacherService.getById(teacherId);
            EvaluationActivityTeacher at = new EvaluationActivityTeacher();
            at.setActivityId(activity.getId());
            at.setTeacherId(teacherId);
            at.setTeacherName(teacher != null ? teacher.getTname() : null);
            at.setCreateTime(LocalDateTime.now());
            activityTeacherMapper.insert(at);
        }

        return activity;
    }

    @Transactional
    public boolean endActivity(Integer id) {
        EvaluationActivity activity = this.getById(id);
        if (activity == null) {
            throw new IllegalArgumentException("活动不存在");
        }
        if ("ended".equals(activity.getStatus())) {
            throw new IllegalStateException("活动已结束");
        }
        activity.setStatus("ended");
        activity.setEndTime(LocalDateTime.now());
        return this.updateById(activity);
    }

    public List<EvaluationDimension> getDimensions(Integer activityId) {
        return dimensionMapper.selectList(
                new LambdaQueryWrapper<EvaluationDimension>()
                        .eq(EvaluationDimension::getActivityId, activityId)
                        .orderByAsc(EvaluationDimension::getSortOrder));
    }

    public List<EvaluationActivityTeacher> getTeachers(Integer activityId) {
        return activityTeacherMapper.selectList(
                new LambdaQueryWrapper<EvaluationActivityTeacher>()
                        .eq(EvaluationActivityTeacher::getActivityId, activityId));
    }

    public boolean isActivityOngoing(Integer activityId) {
        EvaluationActivity activity = this.getById(activityId);
        if (activity == null) return false;
        if (!"ongoing".equals(activity.getStatus())) return false;
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(activity.getStartTime()) && now.isBefore(activity.getEndTime());
    }
}
