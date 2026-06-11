package com.school.controller;

import com.school.entity.Test;
import com.school.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/test")
@CrossOrigin
public class TestController {
    @Autowired private TestService testService;

    @GetMapping("/list") public List<Test> list() { return testService.list(); }
    @PostMapping("/save") public boolean save(@RequestBody Test test) { return testService.saveOrUpdate(test); }
    @DeleteMapping("/delete/{id}") public boolean delete(@PathVariable Integer id) { return testService.removeById(id); }
    @GetMapping("/search") public List<Test> search(@RequestParam String keyword) {
        return testService.lambdaQuery().like(Test::getTtitle, keyword).or().like(Test::getTcontent, keyword).list();
    }
}
