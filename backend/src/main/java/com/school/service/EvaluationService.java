package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.*;
import com.school.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EvaluationService extends ServiceImpl<EvaluationSubmitMapper, EvaluationSubmit> {

    @Autowired
    private EvaluationActivityService activityService;

    @Autowired
    private EvaluationScoreMapper scoreMapper;

    @Autowired
    private EvaluationCommentMapper commentMapper;

    @Autowired
    private EvaluationActivityTeacherMapper activityTeacherMapper;

    @Autowired
    private EvaluationDimensionMapper dimensionMapper;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private UserService userService;

    public boolean hasSubmitted(Integer activityId, Integer studentId, Integer teacherId) {
        Long count = this.count(new LambdaQueryWrapper<EvaluationSubmit>()
                .eq(EvaluationSubmit::getActivityId, activityId)
                .eq(EvaluationSubmit::getStudentId, studentId)
                .eq(EvaluationSubmit::getTeacherId, teacherId));
        return count > 0;
    }

    public Map<Integer, Boolean> getSubmittedStatus(Integer activityId, Integer studentId) {
        List<EvaluationSubmit> submits = this.list(new LambdaQueryWrapper<EvaluationSubmit>()
                .eq(EvaluationSubmit::getActivityId, activityId)
                .eq(EvaluationSubmit::getStudentId, studentId));

        Map<Integer, Boolean> result = new HashMap<>();
        for (EvaluationSubmit submit : submits) {
            result.put(submit.getTeacherId(), true);
        }
        return result;
    }

    @Transactional
    public void submitEvaluation(Integer activityId, Integer studentId, Integer teacherId,
                                  Map<Integer, Integer> scores, String comment) {
        if (!activityService.isActivityOngoing(activityId)) {
            throw new IllegalStateException("评教活动已结束或尚未开始");
        }

        if (hasSubmitted(activityId, studentId, teacherId)) {
            throw new IllegalStateException("您已对该教师进行过评价，请勿重复提交");
        }

        List<EvaluationDimension> dimensions = activityService.getDimensions(activityId);
        if (dimensions.isEmpty()) {
            throw new IllegalArgumentException("评教维度配置异常");
        }

        List<EvaluationActivityTeacher> activityTeachers = activityService.getTeachers(activityId);
        boolean validTeacher = activityTeachers.stream()
                .anyMatch(t -> t.getTeacherId().equals(teacherId));
        if (!validTeacher) {
            throw new IllegalArgumentException("该教师不在本次评教范围内");
        }

        if (scores == null || scores.size() != dimensions.size()) {
            throw new IllegalArgumentException("请完成所有维度的评分");
        }

        for (EvaluationDimension dim : dimensions) {
            Integer score = scores.get(dim.getId());
            if (score == null || score < 1 || score > 5) {
                throw new IllegalArgumentException("评分必须在 1-5 分之间");
            }
        }

        String anonymousToken = generateAnonymousToken();

        EvaluationSubmit submit = new EvaluationSubmit();
        submit.setActivityId(activityId);
        submit.setStudentId(studentId);
        submit.setTeacherId(teacherId);
        submit.setAnonymousToken(anonymousToken);
        submit.setSubmitTime(LocalDateTime.now());
        this.save(submit);

        for (EvaluationDimension dim : dimensions) {
            EvaluationScore es = new EvaluationScore();
            es.setActivityId(activityId);
            es.setTeacherId(teacherId);
            es.setDimensionId(dim.getId());
            es.setScore(scores.get(dim.getId()));
            es.setAnonymousToken(anonymousToken);
            es.setCreateTime(LocalDateTime.now());
            scoreMapper.insert(es);
        }

        if (comment != null && !comment.trim().isEmpty()) {
            EvaluationComment ec = new EvaluationComment();
            ec.setActivityId(activityId);
            ec.setTeacherId(teacherId);
            ec.setComment(comment.trim());
            ec.setAnonymousToken(anonymousToken);
            ec.setCreateTime(LocalDateTime.now());
            commentMapper.insert(ec);
        }
    }

    private String generateAnonymousToken() {
        return "tok_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
    }

    public Map<String, Object> getTeacherResult(Integer activityId, Integer teacherId) {
        EvaluationActivity activity = activityService.getById(activityId);
        if (activity == null) {
            throw new IllegalArgumentException("活动不存在");
        }

        List<EvaluationDimension> dimensions = activityService.getDimensions(activityId);

        List<EvaluationScore> scores = scoreMapper.selectList(
                new LambdaQueryWrapper<EvaluationScore>()
                        .eq(EvaluationScore::getActivityId, activityId)
                        .eq(EvaluationScore::getTeacherId, teacherId));

        Set<String> uniqueTokens = scores.stream()
                .map(EvaluationScore::getAnonymousToken)
                .collect(Collectors.toSet());
        int evaluateCount = uniqueTokens.size();

        Map<Integer, List<Integer>> dimensionScores = new HashMap<>();
        for (EvaluationScore s : scores) {
            dimensionScores.computeIfAbsent(s.getDimensionId(), k -> new ArrayList<>())
                    .add(s.getScore());
        }

        List<Map<String, Object>> dimensionStats = new ArrayList<>();
        BigDecimal totalWeighted = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (EvaluationDimension dim : dimensions) {
            List<Integer> dimScores = dimensionScores.getOrDefault(dim.getId(), Collections.emptyList());
            BigDecimal avgScore = BigDecimal.ZERO;
            if (!dimScores.isEmpty()) {
                int sum = dimScores.stream().mapToInt(Integer::intValue).sum();
                avgScore = new BigDecimal(sum)
                        .divide(new BigDecimal(dimScores.size()), 2, RoundingMode.HALF_UP);
            }

            BigDecimal weighted = avgScore.multiply(dim.getWeight())
                    .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            totalWeighted = totalWeighted.add(weighted);
            totalWeight = totalWeight.add(dim.getWeight());

            Map<String, Object> stat = new LinkedHashMap<>();
            stat.put("dimensionId", dim.getId());
            stat.put("dimensionName", dim.getName());
            stat.put("weight", dim.getWeight());
            stat.put("avgScore", avgScore);
            stat.put("evaluateCount", dimScores.size());
            dimensionStats.add(stat);
        }

        BigDecimal overallScore = BigDecimal.ZERO;
        if (totalWeight.compareTo(BigDecimal.ZERO) > 0 && evaluateCount > 0) {
            overallScore = totalWeighted.setScale(2, RoundingMode.HALF_UP);
        }

        List<EvaluationComment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<EvaluationComment>()
                        .eq(EvaluationComment::getActivityId, activityId)
                        .eq(EvaluationComment::getTeacherId, teacherId)
                        .orderByDesc(EvaluationComment::getCreateTime));

        List<String> commentTexts = comments.stream()
                .map(EvaluationComment::getComment)
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("activityId", activityId);
        result.put("teacherId", teacherId);
        result.put("overallScore", overallScore);
        result.put("evaluateCount", evaluateCount);
        result.put("dimensionStats", dimensionStats);
        result.put("comments", commentTexts);
        return result;
    }

    public List<Map<String, Object>> getRanking(Integer activityId) {
        List<EvaluationActivityTeacher> teachers = activityService.getTeachers(activityId);

        List<Map<String, Object>> ranking = new ArrayList<>();
        for (EvaluationActivityTeacher at : teachers) {
            Map<String, Object> result = getTeacherResult(activityId, at.getTeacherId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("teacherId", at.getTeacherId());
            item.put("teacherName", at.getTeacherName());
            item.put("overallScore", result.get("overallScore"));
            item.put("evaluateCount", result.get("evaluateCount"));
            ranking.add(item);
        }

        ranking.sort((a, b) -> {
            BigDecimal sa = (BigDecimal) a.get("overallScore");
            BigDecimal sb = (BigDecimal) b.get("overallScore");
            return sb.compareTo(sa);
        });

        for (int i = 0; i < ranking.size(); i++) {
            ranking.get(i).put("rank", i + 1);
        }

        return ranking;
    }

    public Map<String, Object> getStudentActivitiesWithStatus(Integer studentId) {
        User student = userService.getById(studentId);
        if (student == null || student.getClassId() == null) {
            throw new IllegalArgumentException("学生信息异常");
        }

        List<EvaluationActivity> activities = activityService.listByStudentClass(student.getClassId());

        List<Map<String, Object>> result = new ArrayList<>();
        for (EvaluationActivity activity : activities) {
            Map<Integer, Boolean> submittedMap = getSubmittedStatus(activity.getId(), studentId);
            List<EvaluationActivityTeacher> teachers = activityService.getTeachers(activity.getId());

            int totalTeachers = teachers.size();
            int submittedCount = (int) teachers.stream()
                    .filter(t -> submittedMap.getOrDefault(t.getTeacherId(), false))
                    .count();

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", activity.getId());
            item.put("title", activity.getTitle());
            item.put("description", activity.getDescription());
            item.put("startTime", activity.getStartTime());
            item.put("endTime", activity.getEndTime());
            item.put("status", activity.getStatus());
            item.put("totalTeachers", totalTeachers);
            item.put("submittedCount", submittedCount);
            item.put("allSubmitted", totalTeachers > 0 && submittedCount == totalTeachers);
            item.put("isOngoing", "ongoing".equals(activity.getStatus())
                    && LocalDateTime.now().isAfter(activity.getStartTime())
                    && LocalDateTime.now().isBefore(activity.getEndTime()));
            result.add(item);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("activities", result);
        return response;
    }

    public List<Map<String, Object>> getTeacherListWithStatus(Integer activityId, Integer studentId) {
        List<EvaluationActivityTeacher> teachers = activityService.getTeachers(activityId);
        Map<Integer, Boolean> submittedMap = getSubmittedStatus(activityId, studentId);
        List<EvaluationDimension> dimensions = activityService.getDimensions(activityId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (EvaluationActivityTeacher at : teachers) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("teacherId", at.getTeacherId());
            item.put("teacherName", at.getTeacherName());
            item.put("submitted", submittedMap.getOrDefault(at.getTeacherId(), false));
            result.add(item);
        }
        return result;
    }

    public Map<String, Object> getEvaluateFormData(Integer activityId, Integer teacherId) {
        List<EvaluationDimension> dimensions = activityService.getDimensions(activityId);
        EvaluationActivityTeacher teacher = activityTeacherMapper.selectOne(
                new LambdaQueryWrapper<EvaluationActivityTeacher>()
                        .eq(EvaluationActivityTeacher::getActivityId, activityId)
                        .eq(EvaluationActivityTeacher::getTeacherId, teacherId));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dimensions", dimensions);
        result.put("teacherName", teacher != null ? teacher.getTeacherName() : "");
        return result;
    }
}
