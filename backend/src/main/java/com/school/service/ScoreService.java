package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.Score;
import com.school.entity.ScoreConfig;
import com.school.entity.User;
import com.school.mapper.ScoreMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScoreService extends ServiceImpl<ScoreMapper, Score> {

    @Autowired
    private ScoreConfigService scoreConfigService;

    @Autowired
    private UserService userService;

    public List<Score> getScoresByCourseAndClass(Integer courseId, Integer classId) {
        return this.list(new LambdaQueryWrapper<Score>()
                .eq(Score::getCourseId, courseId)
                .eq(Score::getClassId, classId)
                .orderByAsc(Score::getStudentNo));
    }

    public List<Score> getScoresByStudent(Integer studentId) {
        return this.list(new LambdaQueryWrapper<Score>()
                .eq(Score::getStudentId, studentId));
    }

    @Transactional
    public Score saveOrUpdateScore(Score score) {
        ScoreConfig config = scoreConfigService.getConfig(score.getCourseId(), score.getClassId());

        if (config.getIsLocked() == 1) {
            throw new IllegalStateException("成绩已锁定，无法修改");
        }

        calculateTotalAndGrade(score, config);

        if (score.getId() == null) {
            Score exist = this.getOne(new LambdaQueryWrapper<Score>()
                    .eq(Score::getCourseId, score.getCourseId())
                    .eq(Score::getClassId, score.getClassId())
                    .eq(Score::getStudentId, score.getStudentId()));
            if (exist != null) {
                score.setId(exist.getId());
            }
        }

        this.saveOrUpdate(score);
        refreshRank(score.getCourseId(), score.getClassId());

        return this.getById(score.getId());
    }

    @Transactional
    public List<Score> batchSaveScores(Integer courseId, Integer classId, List<Score> scores) {
        ScoreConfig config = scoreConfigService.getConfig(courseId, classId);

        if (config.getIsLocked() == 1) {
            throw new IllegalStateException("成绩已锁定，无法修改");
        }

        List<User> students = userService.lambdaQuery()
                .eq(User::getClassId, classId)
                .list();
        Map<Integer, User> studentMap = students.stream()
                .collect(Collectors.toMap(User::getUid, u -> u));

        for (Score score : scores) {
            score.setCourseId(courseId);
            score.setClassId(classId);

            User student = studentMap.get(score.getStudentId());
            if (student != null) {
                score.setStudentName(student.getUsername());
                score.setStudentNo(student.getUserno());
            }

            calculateTotalAndGrade(score, config);

            Score exist = this.getOne(new LambdaQueryWrapper<Score>()
                    .eq(Score::getCourseId, courseId)
                    .eq(Score::getClassId, classId)
                    .eq(Score::getStudentId, score.getStudentId()));
            if (exist != null) {
                score.setId(exist.getId());
            }
            this.saveOrUpdate(score);
        }

        refreshRank(courseId, classId);
        return getScoresByCourseAndClass(courseId, classId);
    }

    @Transactional
    public void initScoresForClass(Integer courseId, Integer classId) {
        ScoreConfig config = scoreConfigService.getConfig(courseId, classId);

        List<User> students = userService.lambdaQuery()
                .eq(User::getClassId, classId)
                .list();

        for (User student : students) {
            Score exist = this.getOne(new LambdaQueryWrapper<Score>()
                    .eq(Score::getCourseId, courseId)
                    .eq(Score::getClassId, classId)
                    .eq(Score::getStudentId, student.getUid()));
            if (exist == null) {
                Score score = new Score();
                score.setCourseId(courseId);
                score.setClassId(classId);
                score.setStudentId(student.getUid());
                score.setStudentName(student.getUsername());
                score.setStudentNo(student.getUserno());
                score.setStatus("unrecorded");
                this.save(score);
            }
        }
    }

    private void calculateTotalAndGrade(Score score, ScoreConfig config) {
        if ("absent".equals(score.getStatus())) {
            score.setTotalScore(BigDecimal.ZERO);
            score.setGrade("不及格");
            return;
        }

        boolean allNull = score.getRegularScore() == null && score.getMidtermScore() == null && score.getFinalScore() == null;
        if (allNull) {
            score.setStatus("unrecorded");
            score.setTotalScore(null);
            score.setGrade(null);
            return;
        }

        score.setStatus("normal");

        BigDecimal regular = score.getRegularScore() != null ? score.getRegularScore() : BigDecimal.ZERO;
        BigDecimal midterm = score.getMidtermScore() != null ? score.getMidtermScore() : BigDecimal.ZERO;
        BigDecimal finalScore = score.getFinalScore() != null ? score.getFinalScore() : BigDecimal.ZERO;

        BigDecimal regularPart = regular.multiply(config.getRegularWeight())
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal midtermPart = midterm.multiply(config.getMidtermWeight())
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal finalPart = finalScore.multiply(config.getFinalWeight())
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        BigDecimal total = regularPart.add(midtermPart).add(finalPart);
        total = total.setScale(config.getScorePrecision(), RoundingMode.HALF_UP);

        score.setTotalScore(total);
        score.setGrade(determineGrade(total, config));
    }

    private String determineGrade(BigDecimal total, ScoreConfig config) {
        if (total.compareTo(config.getGradeExcellent()) >= 0) return "优";
        if (total.compareTo(config.getGradeGood()) >= 0) return "良";
        if (total.compareTo(config.getGradeMedium()) >= 0) return "中";
        if (total.compareTo(config.getGradePass()) >= 0) return "及格";
        return "不及格";
    }

    @Transactional
    public void refreshRank(Integer courseId, Integer classId) {
        List<Score> scores = this.list(new LambdaQueryWrapper<Score>()
                .eq(Score::getCourseId, courseId)
                .eq(Score::getClassId, classId)
                .eq(Score::getStatus, "normal")
                .isNotNull(Score::getTotalScore)
                .orderByDesc(Score::getTotalScore));

        if (scores.isEmpty()) return;

        int rank = 1;
        BigDecimal prevScore = null;
        int sameScoreCount = 0;

        for (int i = 0; i < scores.size(); i++) {
            Score s = scores.get(i);
            if (prevScore == null || s.getTotalScore().compareTo(prevScore) < 0) {
                rank = i + 1;
                prevScore = s.getTotalScore();
                sameScoreCount = 1;
            } else {
                sameScoreCount++;
            }
            s.setClassRank(rank);
            this.updateById(s);
        }
    }

    public Map<String, Object> getStatistics(Integer courseId, Integer classId) {
        ScoreConfig config = scoreConfigService.getConfig(courseId, classId);
        List<Score> scores = this.list(new LambdaQueryWrapper<Score>()
                .eq(Score::getCourseId, courseId)
                .eq(Score::getClassId, classId)
                .eq(Score::getStatus, "normal")
                .isNotNull(Score::getTotalScore));

        Map<String, Object> result = new HashMap<>();

        if (scores.isEmpty()) {
            result.put("totalStudents", 0);
            result.put("maxScore", null);
            result.put("minScore", null);
            result.put("avgScore", null);
            result.put("passRate", null);
            result.put("scoreDistribution", new HashMap<>());
            result.put("gradeDistribution", new HashMap<>());
            return result;
        }

        List<BigDecimal> totals = scores.stream()
                .map(Score::getTotalScore)
                .filter(Objects::nonNull)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        int totalStudents = scores.size();
        BigDecimal maxScore = totals.get(0);
        BigDecimal minScore = totals.get(totals.size() - 1);
        BigDecimal avgScore = totals.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(totalStudents), config.getScorePrecision(), RoundingMode.HALF_UP);

        long passCount = totals.stream()
                .filter(s -> s.compareTo(config.getGradePass()) >= 0)
                .count();
        BigDecimal passRate = new BigDecimal(passCount)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(totalStudents), 2, RoundingMode.HALF_UP);

        Map<String, Integer> scoreDistribution = new LinkedHashMap<>();
        scoreDistribution.put("[90,100]", 0);
        scoreDistribution.put("[80,90)", 0);
        scoreDistribution.put("[70,80)", 0);
        scoreDistribution.put("[60,70)", 0);
        scoreDistribution.put("[0,60)", 0);

        for (BigDecimal s : totals) {
            if (s.compareTo(new BigDecimal("90")) >= 0) scoreDistribution.merge("[90,100]", 1, Integer::sum);
            else if (s.compareTo(new BigDecimal("80")) >= 0) scoreDistribution.merge("[80,90)", 1, Integer::sum);
            else if (s.compareTo(new BigDecimal("70")) >= 0) scoreDistribution.merge("[70,80)", 1, Integer::sum);
            else if (s.compareTo(new BigDecimal("60")) >= 0) scoreDistribution.merge("[60,70)", 1, Integer::sum);
            else scoreDistribution.merge("[0,60)", 1, Integer::sum);
        }

        Map<String, Integer> gradeDistribution = new LinkedHashMap<>();
        gradeDistribution.put("优", 0);
        gradeDistribution.put("良", 0);
        gradeDistribution.put("中", 0);
        gradeDistribution.put("及格", 0);
        gradeDistribution.put("不及格", 0);

        for (Score s : scores) {
            if (s.getGrade() != null) {
                gradeDistribution.merge(s.getGrade(), 1, Integer::sum);
            }
        }

        result.put("totalStudents", totalStudents);
        result.put("maxScore", maxScore);
        result.put("minScore", minScore);
        result.put("avgScore", avgScore);
        result.put("passRate", passRate);
        result.put("scoreDistribution", scoreDistribution);
        result.put("gradeDistribution", gradeDistribution);

        return result;
    }
}
