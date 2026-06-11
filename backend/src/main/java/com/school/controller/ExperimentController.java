package com.school.controller;

import com.school.entity.Experiment;
import com.school.service.ExperimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/experiment")
@CrossOrigin
public class ExperimentController {
    @Autowired private ExperimentService experimentService;

    @GetMapping("/list") public List<Experiment> list() { return experimentService.list(); }
    @PostMapping("/save") public boolean save(@RequestBody Experiment experiment) { return experimentService.saveOrUpdate(experiment); }
    @DeleteMapping("/delete/{id}") public boolean delete(@PathVariable Integer id) { return experimentService.removeById(id); }
}
