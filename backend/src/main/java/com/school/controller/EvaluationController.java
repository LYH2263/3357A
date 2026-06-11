package com.school.controller;

import com.school.entity.EvaluationActivity;
import com.school.entity.EvaluationDimension;
import com.school.service.EvaluationActivityService;
import com.school.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evaluation")
@CrossOrigin
public class EvaluationController {

    @Autowired
    private EvaluationActivityService activityService;

    @Autowired
    private EvaluationService evaluationService;

    // ==================== 活动管理（教师/管理员端） ====================

    @GetMapping("/activity/list")
    public List<EvaluationActivity> listActivities() {
        return activityService.listAll();
    }

    @GetMapping("/activity/detail")
    public Map<String, Object> getActivityDetail(@RequestParam Integer id) {
        return activityService.getDetail(id);
    }

    @PostMapping("/activity/create")
    public EvaluationActivity createActivity(@RequestBody Map<String, Object> params) {
        EvaluationActivity activity = new EvaluationActivity();
        activity.setTitle((String) params.get("title"));
        activity.setDescription((String) params.get("description"));

        String startTimeStr = (String) params.get("startTime");
        String endTimeStr = (String) params.get("endTime");
        activity.setStartTime(java.time.LocalDateTime.parse(startTimeStr));
        activity.setEndTime(java.time.LocalDateTime.parse(endTimeStr));

        Object creatorId = params.get("creatorId");
        if (creatorId != null) {
            activity.setCreatorId(((Number) creatorId).intValue());
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dimensionsRaw = (List<Map<String, Object>>) params.get("dimensions");
        List<EvaluationDimension> dimensions = dimensionsRaw.stream().map(d -> {
            EvaluationDimension dim = new EvaluationDimension();
            dim.setName((String) d.get("name"));
            dim.setWeight(new java.math.BigDecimal(d.get("weight").toString()));
            return dim;
        }).collect(java.util.stream.Collectors.toList());

        @SuppressWarnings("unchecked")
        List<Integer> classIds = (List<Integer>) params.get("classIds");

        @SuppressWarnings("unchecked")
        List<Integer> teacherIds = (List<Integer>) params.get("teacherIds");

        return activityService.createActivity(activity, dimensions, classIds, teacherIds);
    }

    @PostMapping("/activity/end")
    public boolean endActivity(@RequestParam Integer id) {
        return activityService.endActivity(id);
    }

    // ==================== 学生端接口 ====================

    @GetMapping("/student/activities")
    public Map<String, Object> getStudentActivities(@RequestParam Integer studentId) {
        return evaluationService.getStudentActivitiesWithStatus(studentId);
    }

    @GetMapping("/student/teachers")
    public List<Map<String, Object>> getTeacherList(
            @RequestParam Integer activityId,
            @RequestParam Integer studentId) {
        return evaluationService.getTeacherListWithStatus(activityId, studentId);
    }

    @GetMapping("/student/form")
    public Map<String, Object> getEvaluateForm(
            @RequestParam Integer activityId,
            @RequestParam Integer teacherId) {
        return evaluationService.getEvaluateFormData(activityId, teacherId);
    }

    @PostMapping("/student/submit")
    public void submitEvaluation(@RequestBody Map<String, Object> params) {
        Integer activityId = ((Number) params.get("activityId")).intValue();
        Integer studentId = ((Number) params.get("studentId")).intValue();
        Integer teacherId = ((Number) params.get("teacherId")).intValue();
        String comment = (String) params.get("comment");

        @SuppressWarnings("unchecked")
        Map<String, Object> scoresRaw = (Map<String, Object>) params.get("scores");
        Map<Integer, Integer> scores = new java.util.HashMap<>();
        for (Map.Entry<String, Object> entry : scoresRaw.entrySet()) {
            scores.put(Integer.parseInt(entry.getKey()), ((Number) entry.getValue()).intValue());
        }

        evaluationService.submitEvaluation(activityId, studentId, teacherId, scores, comment);
    }

    @GetMapping("/student/check-submitted")
    public boolean checkSubmitted(
            @RequestParam Integer activityId,
            @RequestParam Integer studentId,
            @RequestParam Integer teacherId) {
        return evaluationService.hasSubmitted(activityId, studentId, teacherId);
    }

    // ==================== 结果查询（教师端 / 管理端） ====================

    @GetMapping("/teacher/result")
    public Map<String, Object> getTeacherResult(
            @RequestParam Integer activityId,
            @RequestParam Integer teacherId) {
        return evaluationService.getTeacherResult(activityId, teacherId);
    }

    @GetMapping("/admin/ranking")
    public List<Map<String, Object>> getRanking(@RequestParam Integer activityId) {
        return evaluationService.getRanking(activityId);
    }

    @GetMapping("/activity/teachers")
    public List<?> getActivityTeachers(@RequestParam Integer activityId) {
        return activityService.getTeachers(activityId);
    }

    @GetMapping("/activity/dimensions")
    public List<EvaluationDimension> getDimensions(@RequestParam Integer activityId) {
        return activityService.getDimensions(activityId);
    }
}
