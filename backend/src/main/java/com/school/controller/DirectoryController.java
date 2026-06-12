package com.school.controller;

import com.school.dto.*;
import com.school.entity.Classes;
import com.school.service.DirectoryService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/directory")
@CrossOrigin
public class DirectoryController {

    @Autowired
    private DirectoryService directoryService;

    @GetMapping("/teachers")
    public Map<String, Object> getTeachers(
            @ModelAttribute DirectoryQuery query,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {

        if (role == null || role.isEmpty()) {
            role = "teacher";
        }

        DirectoryPageResult<TeacherCard> result = directoryService.queryTeachers(query, role, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", result);
        return response;
    }

    @GetMapping("/students")
    public Map<String, Object> getStudents(
            @ModelAttribute DirectoryQuery query,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {

        if (role == null || role.isEmpty()) {
            role = "teacher";
        }

        DirectoryPageResult<StudentCard> result = directoryService.queryStudents(query, role, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", result);
        return response;
    }

    @GetMapping("/teacher/{id}")
    public Map<String, Object> getTeacherDetail(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {

        if (role == null || role.isEmpty()) {
            role = "teacher";
        }

        TeacherDetail detail = directoryService.getTeacherDetail(id, role, userId);

        Map<String, Object> response = new HashMap<>();
        if (detail == null) {
            response.put("success", false);
            response.put("message", "未找到该教师信息或无权限查看");
        } else {
            response.put("success", true);
            response.put("data", detail);
        }
        return response;
    }

    @GetMapping("/student/{id}")
    public Map<String, Object> getStudentDetail(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {

        if (role == null || role.isEmpty()) {
            role = "teacher";
        }

        StudentDetail detail = directoryService.getStudentDetail(id, role, userId);

        Map<String, Object> response = new HashMap<>();
        if (detail == null) {
            response.put("success", false);
            response.put("message", "未找到该学生信息或无权限查看");
        } else {
            response.put("success", true);
            response.put("data", detail);
        }
        return response;
    }

    @GetMapping("/classes")
    public Map<String, Object> getClasses() {
        List<Classes> classes = directoryService.getAllClasses();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", classes);
        return response;
    }

    @GetMapping("/expertise")
    public Map<String, Object> getExpertise() {
        List<Map<String, String>> expertise = directoryService.getAllExpertise();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", expertise);
        return response;
    }

    @PostMapping("/export/teachers/vcf")
    public void exportTeachersVcf(
            @RequestBody DirectoryQuery query,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            HttpServletResponse response) throws Exception {

        if (role == null || role.isEmpty()) {
            role = "teacher";
        }

        String vcfContent = directoryService.exportTeachersToVcf(query, role, userId);

        response.setContentType("text/vcard;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String fileName = URLEncoder.encode("教师通讯录.vcf", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        try (OutputStream os = response.getOutputStream()) {
            os.write(vcfContent.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
    }

    @PostMapping("/export/students/vcf")
    public void exportStudentsVcf(
            @RequestBody DirectoryQuery query,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            HttpServletResponse response) throws Exception {

        if (role == null || role.isEmpty()) {
            role = "teacher";
        }

        String vcfContent = directoryService.exportStudentsToVcf(query, role, userId);

        response.setContentType("text/vcard;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String fileName = URLEncoder.encode("学生通讯录.vcf", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        try (OutputStream os = response.getOutputStream()) {
            os.write(vcfContent.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
    }

    @PostMapping("/export/teachers/csv")
    public void exportTeachersCsv(
            @RequestBody DirectoryQuery query,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            HttpServletResponse response) throws Exception {

        if (role == null || role.isEmpty()) {
            role = "teacher";
        }

        String csvContent = directoryService.exportTeachersToCsv(query, role, userId);

        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String fileName = URLEncoder.encode("教师通讯录.csv", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
        try (OutputStream os = response.getOutputStream()) {
            os.write(bom);
            os.write(csvContent.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
    }

    @PostMapping("/export/students/csv")
    public void exportStudentsCsv(
            @RequestBody DirectoryQuery query,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            HttpServletResponse response) throws Exception {

        if (role == null || role.isEmpty()) {
            role = "teacher";
        }

        String csvContent = directoryService.exportStudentsToCsv(query, role, userId);

        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String fileName = URLEncoder.encode("学生通讯录.csv", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
        try (OutputStream os = response.getOutputStream()) {
            os.write(bom);
            os.write(csvContent.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
    }
}
