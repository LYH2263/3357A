package com.school.controller;

import com.school.dto.RepositoryFileDto;
import com.school.dto.RepositoryFolderTree;
import com.school.dto.VisibilityConfig;
import com.school.entity.RepositoryFile;
import com.school.entity.RepositoryFolder;
import com.school.service.RepositoryService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/repository")
@CrossOrigin
public class RepositoryController {

    @Autowired
    private RepositoryService repositoryService;

    @Value("${spring.servlet.multipart.location:/app/uploads/}")
    private String uploadPath;

    @GetMapping("/folder/tree")
    public Map<String, Object> getFolderTree(@RequestParam(required = false) Integer studentId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<RepositoryFolderTree> tree;
            if (studentId != null) {
                tree = repositoryService.getFolderTreeWithPermission(studentId);
            } else {
                tree = repositoryService.getFolderTree();
            }
            result.put("success", true);
            result.put("data", tree);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/folder/create")
    public Map<String, Object> createFolder(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            RepositoryFolder folder = new RepositoryFolder();
            folder.setName((String) params.get("name"));
            folder.setParentId(params.get("parentId") != null ? ((Number) params.get("parentId")).intValue() : 0);
            folder.setVisibilityType((String) params.get("visibilityType"));
            folder.setCreatorId(params.get("creatorId") != null ? ((Number) params.get("creatorId")).intValue() : null);
            folder.setCreatorName((String) params.get("creatorName"));

            @SuppressWarnings("unchecked")
            List<Integer> classIds = (List<Integer>) params.get("classIds");

            RepositoryFolder created = repositoryService.createFolder(folder, classIds);
            result.put("success", true);
            result.put("data", created);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/folder/rename")
    public Map<String, Object> renameFolder(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer id = ((Number) params.get("id")).intValue();
            String newName = (String) params.get("name");
            boolean success = repositoryService.renameFolder(id, newName);
            result.put("success", success);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/file/rename")
    public Map<String, Object> renameFile(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer id = ((Number) params.get("id")).intValue();
            String newName = (String) params.get("name");
            boolean success = repositoryService.renameFile(id, newName);
            result.put("success", success);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/folder/delete/{id}")
    public Map<String, Object> deleteFolder(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = repositoryService.deleteFolder(id);
            result.put("success", success);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/folder/move")
    public Map<String, Object> moveFolder(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer sourceId = ((Number) params.get("sourceId")).intValue();
            Integer targetParentId = params.get("targetParentId") != null ? ((Number) params.get("targetParentId")).intValue() : 0;
            boolean success = repositoryService.moveFolder(sourceId, targetParentId);
            result.put("success", success);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/folder/visibility/{id}")
    public Map<String, Object> setFolderVisibility(@PathVariable Integer id, @RequestBody VisibilityConfig config) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = repositoryService.setFolderVisibility(id, config);
            result.put("success", success);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/file/upload")
    public Map<String, Object> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Integer folderId,
            @RequestParam(required = false, defaultValue = "INHERIT") String visibilityType,
            @RequestParam(required = false) String classIds,
            @RequestParam(required = false) Integer uploaderId,
            @RequestParam(required = false) String uploaderName) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Integer> classIdList = null;
            if (classIds != null && !classIds.trim().isEmpty()) {
                classIdList = java.util.Arrays.stream(classIds.split(","))
                        .map(Integer::parseInt)
                        .collect(java.util.stream.Collectors.toList());
            }

            RepositoryFile uploaded = repositoryService.uploadFile(
                    file, folderId, visibilityType, classIdList, uploaderId, uploaderName
            );
            result.put("success", true);
            result.put("data", uploaded);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/file/move")
    public Map<String, Object> moveFile(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer fileId = ((Number) params.get("fileId")).intValue();
            Integer targetFolderId = params.get("targetFolderId") != null ? ((Number) params.get("targetFolderId")).intValue() : 0;
            boolean success = repositoryService.moveFile(fileId, targetFolderId);
            result.put("success", success);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/file/delete/{id}")
    public Map<String, Object> deleteFile(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = repositoryService.deleteFile(id);
            result.put("success", success);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/file/visibility/{id}")
    public Map<String, Object> setFileVisibility(@PathVariable Integer id, @RequestBody VisibilityConfig config) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = repositoryService.setFileVisibility(id, config);
            result.put("success", success);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/file/list")
    public Map<String, Object> getFilesByFolder(
            @RequestParam(required = false) Integer folderId,
            @RequestParam(required = false) Integer studentId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fileType) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<RepositoryFileDto> files = repositoryService.getFilesByFolder(folderId, studentId, keyword, fileType);
            result.put("success", true);
            result.put("data", files);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/file/download/{id}")
    public void downloadFile(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        RepositoryFileDto file = repositoryService.getFileForDownload(id);
        if (file == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String filePath = file.getFilePath();
        if (filePath != null && filePath.startsWith("/uploads/")) {
            File physicalFile = new File(uploadPath + filePath.substring(9));
            if (!physicalFile.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.setContentType("application/octet-stream");
            String encodedName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8.name()).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedName);
            response.setContentLengthLong(physicalFile.length());

            try (FileInputStream fis = new FileInputStream(physicalFile);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    @PostMapping("/file/pin/{id}")
    public Map<String, Object> togglePin(@PathVariable Integer id, @RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer studentId = ((Number) params.get("studentId")).intValue();
            boolean pinned = repositoryService.togglePin(id, studentId);
            result.put("success", true);
            result.put("pinned", pinned);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/file/pinned")
    public Map<String, Object> getPinnedFiles(@RequestParam Integer studentId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<RepositoryFileDto> files = repositoryService.getPinnedFiles(studentId);
            result.put("success", true);
            result.put("data", files);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> config = repositoryService.getAllowedConfig();
            result.put("success", true);
            result.put("data", config);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
