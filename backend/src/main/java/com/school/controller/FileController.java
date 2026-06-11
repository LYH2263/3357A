package com.school.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
@CrossOrigin
public class FileController {

    private final String uploadPath = "/app/uploads/";

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        if (file.isEmpty()) {
            result.put("success", false);
            result.put("message", "文件不能为空");
            return result;
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + extension;

        File dest = new File(uploadPath + newFilename);
        try {
            file.transferTo(dest);
            result.put("success", true);
            result.put("filename", newFilename);
            result.put("url", "/uploads/" + newFilename);
        } catch (IOException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "文件保存失败: " + e.getMessage());
        }

        return result;
    }
}
