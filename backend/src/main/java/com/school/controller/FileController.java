package com.school.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/file")
@CrossOrigin
public class FileController {

    private final String uploadPath = "/app/uploads/";
    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    ));
    private static final Set<String> ALLOWED_IMAGE_EXT = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp"
    ));
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        if (file.isEmpty()) {
            result.put("success", false);
            result.put("message", "文件不能为空");
            return result;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            result.put("success", false);
            result.put("message", "文件大小不能超过 100MB");
            return result;
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + extension;

        File dest = new File(uploadPath + newFilename);
        try {
            file.transferTo(dest);
            result.put("success", true);
            result.put("filename", newFilename);
            result.put("url", "/uploads/" + newFilename);
            result.put("originalName", originalFilename);
            result.put("size", file.getSize());
        } catch (IOException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "文件保存失败: " + e.getMessage());
        }

        return result;
    }

    @PostMapping("/upload-image")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        if (file.isEmpty()) {
            result.put("success", false);
            result.put("message", "图片不能为空");
            return result;
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        String contentType = file.getContentType();
        boolean typeValid = false;
        if (contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            typeValid = true;
        }
        if (!typeValid && ALLOWED_IMAGE_EXT.contains(extension)) {
            typeValid = true;
        }
        if (!typeValid) {
            result.put("success", false);
            result.put("message", "不支持的图片类型，仅支持 JPG/PNG/GIF/WEBP/BMP");
            return result;
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            result.put("success", false);
            result.put("message", "图片大小不能超过 10MB");
            return result;
        }

        String newFilename = UUID.randomUUID().toString() + extension;
        File dest = new File(uploadPath + newFilename);
        try {
            file.transferTo(dest);
            result.put("success", true);
            result.put("filename", newFilename);
            result.put("url", "/uploads/" + newFilename);
            result.put("originalName", originalFilename);
            result.put("size", file.getSize());
        } catch (IOException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "图片保存失败: " + e.getMessage());
        }

        return result;
    }

    @PostMapping("/batch-upload-image")
    public Map<String, Object> batchUploadImage(@RequestParam("files") MultipartFile[] files) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> uploaded = new ArrayList<>();
        List<Map<String, Object>> failed = new ArrayList<>();

        if (files == null || files.length == 0) {
            result.put("success", false);
            result.put("message", "请选择要上传的图片");
            return result;
        }

        for (MultipartFile file : files) {
            Map<String, Object> item = new HashMap<>();
            String originalFilename = file.getOriginalFilename();
            item.put("originalName", originalFilename);

            if (file.isEmpty()) {
                item.put("success", false);
                item.put("message", "文件为空");
                failed.add(item);
                continue;
            }

            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }
            String contentType = file.getContentType();
            boolean typeValid = false;
            if (contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
                typeValid = true;
            }
            if (!typeValid && ALLOWED_IMAGE_EXT.contains(extension)) {
                typeValid = true;
            }
            if (!typeValid) {
                item.put("success", false);
                item.put("message", "不支持的图片类型");
                failed.add(item);
                continue;
            }

            if (file.getSize() > MAX_IMAGE_SIZE) {
                item.put("success", false);
                item.put("message", "图片超过10MB");
                failed.add(item);
                continue;
            }

            String newFilename = UUID.randomUUID().toString() + extension;
            File dest = new File(uploadPath + newFilename);
            try {
                file.transferTo(dest);
                item.put("success", true);
                item.put("filename", newFilename);
                item.put("url", "/uploads/" + newFilename);
                item.put("size", file.getSize());
                uploaded.add(item);
            } catch (IOException e) {
                item.put("success", false);
                item.put("message", "保存失败: " + e.getMessage());
                failed.add(item);
            }
        }

        result.put("success", true);
        result.put("uploaded", uploaded);
        result.put("failed", failed);
        result.put("total", files.length);
        result.put("successCount", uploaded.size());
        result.put("failCount", failed.size());
        return result;
    }
}
