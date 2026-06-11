package com.school.controller;

import com.school.entity.Teacher;
import com.school.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/teacher")
@CrossOrigin
public class TeacherController {
    @Autowired private TeacherService teacherService;

    @GetMapping("/list") public List<Teacher> list() { return teacherService.list(); }
    @PostMapping("/save") public boolean save(@RequestBody Teacher teacher) { return teacherService.saveOrUpdate(teacher); }
    @DeleteMapping("/delete/{id}") public boolean delete(@PathVariable Integer id) { return teacherService.removeById(id); }
}
