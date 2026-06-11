package com.school.controller;

import com.school.entity.Classes;
import com.school.service.ClassesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/classes")
@CrossOrigin
public class ClassesController {
    @Autowired private ClassesService classesService;

    @GetMapping("/list") public List<Classes> list() { return classesService.list(); }
    @PostMapping("/save") public boolean save(@RequestBody Classes classes) { return classesService.saveOrUpdate(classes); }
    @DeleteMapping("/delete/{id}") public boolean delete(@PathVariable Integer id) { return classesService.removeById(id); }
}
