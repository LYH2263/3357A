package com.school.controller;

import com.school.entity.Course;
import com.school.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/course")
@CrossOrigin
public class CourseController {
    @Autowired private CourseService courseService;

    @GetMapping("/list") public List<Course> list() { return courseService.list(); }
    @PostMapping("/save") public boolean save(@RequestBody Course course) { return courseService.saveOrUpdate(course); }
    @DeleteMapping("/delete/{id}") public boolean delete(@PathVariable Integer id) { return courseService.removeById(id); }
}
