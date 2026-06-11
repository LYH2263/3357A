package com.school.controller;

import com.school.entity.Score;
import com.school.entity.ScoreConfig;
import com.school.service.ScoreConfigService;
import com.school.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/score")
@CrossOrigin
public class ScoreController {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private ScoreConfigService scoreConfigService;

    @GetMapping("/config")
    public ScoreConfig getConfig(@RequestParam Integer courseId, @RequestParam Integer classId) {
        return scoreConfigService.getConfig(courseId, classId);
    }

    @PostMapping("/config/save")
    public boolean saveConfig(@RequestBody ScoreConfig config) {
        return scoreConfigService.saveConfig(config);
    }

    @PostMapping("/config/lock")
    public boolean toggleLock(@RequestParam Integer id, @RequestParam boolean locked) {
        return scoreConfigService.toggleLock(id, locked);
    }

    @GetMapping("/list")
    public List<Score> list(@RequestParam Integer courseId, @RequestParam Integer classId) {
        return scoreService.getScoresByCourseAndClass(courseId, classId);
    }

    @GetMapping("/my")
    public List<Score> myScores(@RequestParam Integer studentId) {
        return scoreService.getScoresByStudent(studentId);
    }

    @PostMapping("/save")
    public Score save(@RequestBody Score score) {
        return scoreService.saveOrUpdateScore(score);
    }

    @PostMapping("/batchSave")
    public List<Score> batchSave(@RequestParam Integer courseId,
                                 @RequestParam Integer classId,
                                 @RequestBody List<Score> scores) {
        return scoreService.batchSaveScores(courseId, classId, scores);
    }

    @PostMapping("/init")
    public void initScores(@RequestParam Integer courseId, @RequestParam Integer classId) {
        scoreService.initScoresForClass(courseId, classId);
    }

    @GetMapping("/statistics")
    public Map<String, Object> statistics(@RequestParam Integer courseId, @RequestParam Integer classId) {
        return scoreService.getStatistics(courseId, classId);
    }
}
