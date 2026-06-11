package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.dto.RepositoryFileDto;
import com.school.dto.RepositoryFolderTree;
import com.school.dto.VisibilityConfig;
import com.school.entity.*;
import com.school.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RepositoryService extends ServiceImpl<RepositoryFolderMapper, RepositoryFolder> {

    @Autowired
    private RepositoryFolderMapper folderMapper;
    @Autowired
    private RepositoryFolderClassMapper folderClassMapper;
    @Autowired
    private RepositoryFileMapper fileMapper;
    @Autowired
    private RepositoryFileClassMapper fileClassMapper;
    @Autowired
    private RepositoryFilePinMapper filePinMapper;
    @Autowired
    private UserMapper userMapper;

    @Value("${spring.servlet.multipart.location:/app/uploads/}")
    private String uploadPath;

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md",
            "zip", "rar", "7z", "tar", "gz",
            "jpg", "jpeg", "png", "gif", "bmp", "webp",
            "mp4", "avi", "mov", "wmv", "flv", "mp3", "wav", "flac",
            "java", "py", "js", "html", "css", "sql", "xml", "json", "csv"
    ));

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<RepositoryFolderTree> getFolderTree() {
        List<RepositoryFolder> allFolders = folderMapper.selectList(null);
        Map<Integer, List<Integer>> folderClassMap = getFolderClassMap();
        Map<Integer, RepositoryFolderTree> treeMap = new HashMap<>();
        List<RepositoryFolderTree> roots = new ArrayList<>();

        for (RepositoryFolder folder : allFolders) {
            RepositoryFolderTree tree = convertToTree(folder, folderClassMap);
            treeMap.put(folder.getId(), tree);
            if (folder.getParentId() == 0) {
                roots.add(tree);
            }
        }

        for (RepositoryFolder folder : allFolders) {
            if (folder.getParentId() != 0) {
                RepositoryFolderTree parent = treeMap.get(folder.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(treeMap.get(folder.getId()));
                }
            }
        }

        sortTree(roots);
        return roots;
    }

    public List<RepositoryFolderTree> getFolderTreeWithPermission(Integer studentId) {
        User student = userMapper.selectById(studentId);
        if (student == null || student.getClassId() == null) {
            return new ArrayList<>();
        }
        Integer classId = student.getClassId();

        List<RepositoryFolderTree> fullTree = getFolderTree();
        return filterTreeByPermission(fullTree, classId);
    }

    private List<RepositoryFolderTree> filterTreeByPermission(List<RepositoryFolderTree> trees, Integer classId) {
        List<RepositoryFolderTree> result = new ArrayList<>();
        for (RepositoryFolderTree tree : trees) {
            boolean hasPermission = checkFolderPermission(tree, classId);
            if (hasPermission) {
                RepositoryFolderTree filtered = new RepositoryFolderTree();
                filtered.setId(tree.getId());
                filtered.setName(tree.getName());
                filtered.setParentId(tree.getParentId());
                filtered.setPath(tree.getPath());
                filtered.setDepth(tree.getDepth());
                filtered.setVisibilityType(tree.getVisibilityType());
                filtered.setClassIds(tree.getClassIds());
                filtered.setCreatorName(tree.getCreatorName());
                filtered.setCreateTime(tree.getCreateTime());

                if (tree.getChildren() != null) {
                    filtered.setChildren(filterTreeByPermission(tree.getChildren(), classId));
                }
                result.add(filtered);
            }
        }
        return result;
    }

    private boolean checkFolderPermission(RepositoryFolderTree folder, Integer classId) {
        if ("ALL".equals(folder.getVisibilityType())) {
            return true;
        }
        if ("CLASSES".equals(folder.getVisibilityType()) && folder.getClassIds() != null) {
            return folder.getClassIds().contains(classId);
        }
        return false;
    }

    private Map<Integer, List<Integer>> getFolderClassMap() {
        List<RepositoryFolderClass> all = folderClassMapper.selectList(null);
        return all.stream()
                .collect(Collectors.groupingBy(
                        RepositoryFolderClass::getFolderId,
                        Collectors.mapping(RepositoryFolderClass::getClassId, Collectors.toList())
                ));
    }

    private Map<Integer, List<Integer>> getFileClassMap() {
        List<RepositoryFileClass> all = fileClassMapper.selectList(null);
        return all.stream()
                .collect(Collectors.groupingBy(
                        RepositoryFileClass::getFileId,
                        Collectors.mapping(RepositoryFileClass::getClassId, Collectors.toList())
                ));
    }

    private RepositoryFolderTree convertToTree(RepositoryFolder folder, Map<Integer, List<Integer>> classMap) {
        RepositoryFolderTree tree = new RepositoryFolderTree();
        tree.setId(folder.getId());
        tree.setName(folder.getName());
        tree.setParentId(folder.getParentId());
        tree.setPath(folder.getPath());
        tree.setDepth(folder.getDepth());
        tree.setVisibilityType(folder.getVisibilityType());
        tree.setClassIds(classMap.get(folder.getId()));
        tree.setCreatorName(folder.getCreatorName());
        tree.setCreateTime(folder.getCreateTime() != null ? folder.getCreateTime().format(FORMATTER) : null);
        return tree;
    }

    private void sortTree(List<RepositoryFolderTree> trees) {
        if (trees == null) return;
        trees.sort(Comparator.comparing(RepositoryFolderTree::getName));
        for (RepositoryFolderTree tree : trees) {
            sortTree(tree.getChildren());
        }
    }

    @Transactional
    public RepositoryFolder createFolder(RepositoryFolder folder, List<Integer> classIds) {
        if (folder.getParentId() == null) {
            folder.setParentId(0);
        }

        RepositoryFolder parent = null;
        if (folder.getParentId() > 0) {
            parent = folderMapper.selectById(folder.getParentId());
            if (parent == null) {
                throw new RuntimeException("父目录不存在");
            }
            folder.setDepth(parent.getDepth() + 1);
            folder.setPath(parent.getPath() + folder.getName() + "/");
        } else {
            folder.setDepth(1);
            folder.setPath("/" + folder.getName() + "/");
        }

        LambdaQueryWrapper<RepositoryFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepositoryFolder::getParentId, folder.getParentId())
                .eq(RepositoryFolder::getName, folder.getName());
        if (folderMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("同级目录下已存在同名文件夹");
        }

        folderMapper.insert(folder);

        if ("CLASSES".equals(folder.getVisibilityType()) && classIds != null && !classIds.isEmpty()) {
            for (Integer classId : classIds) {
                RepositoryFolderClass fc = new RepositoryFolderClass();
                fc.setFolderId(folder.getId());
                fc.setClassId(classId);
                folderClassMapper.insert(fc);
            }
        }

        return folder;
    }

    @Transactional
    public boolean renameFolder(Integer id, String newName) {
        RepositoryFolder folder = folderMapper.selectById(id);
        if (folder == null) {
            throw new RuntimeException("目录不存在");
        }

        LambdaQueryWrapper<RepositoryFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepositoryFolder::getParentId, folder.getParentId())
                .eq(RepositoryFolder::getName, newName)
                .ne(RepositoryFolder::getId, id);
        if (folderMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("同级目录下已存在同名文件夹");
        }

        String oldPath = folder.getPath();
        String oldName = folder.getName();
        String newPath = oldPath.substring(0, oldPath.length() - oldName.length() - 1) + newName + "/";

        folder.setName(newName);
        folder.setPath(newPath);
        folderMapper.updateById(folder);

        updateChildPaths(id, oldPath, newPath);
        return true;
    }

    private void updateChildPaths(Integer parentId, String oldPath, String newPath) {
        List<RepositoryFolder> children = folderMapper.selectList(
                new LambdaQueryWrapper<RepositoryFolder>().eq(RepositoryFolder::getParentId, parentId)
        );
        for (RepositoryFolder child : children) {
            String childOldPath = child.getPath();
            String childNewPath = childOldPath.replace(oldPath, newPath);
            child.setPath(childNewPath);
            folderMapper.updateById(child);
            updateChildPaths(child.getId(), oldPath, newPath);
        }
    }

    @Transactional
    public boolean deleteFolder(Integer id) {
        RepositoryFolder folder = folderMapper.selectById(id);
        if (folder == null) {
            throw new RuntimeException("目录不存在");
        }

        List<Integer> descendantIds = new ArrayList<>();
        collectDescendantFolders(id, descendantIds);
        descendantIds.add(id);

        List<RepositoryFile> filesToDelete = fileMapper.selectList(
                new LambdaQueryWrapper<RepositoryFile>().in(RepositoryFile::getFolderId, descendantIds)
        );

        for (RepositoryFile file : filesToDelete) {
            deletePhysicalFile(file.getFilePath());
            fileMapper.deleteById(file.getId());
        }

        for (Integer folderId : descendantIds) {
            folderMapper.deleteById(folderId);
        }

        return true;
    }

    private void collectDescendantFolders(Integer parentId, List<Integer> ids) {
        List<RepositoryFolder> children = folderMapper.selectList(
                new LambdaQueryWrapper<RepositoryFolder>().eq(RepositoryFolder::getParentId, parentId)
        );
        for (RepositoryFolder child : children) {
            ids.add(child.getId());
            collectDescendantFolders(child.getId(), ids);
        }
    }

    private void deletePhysicalFile(String filePath) {
        if (filePath != null && filePath.startsWith("/uploads/")) {
            File file = new File(uploadPath + filePath.substring(9));
            if (file.exists()) {
                file.delete();
            }
        }
    }

    @Transactional
    public RepositoryFile uploadFile(MultipartFile file, Integer folderId, String visibilityType,
                                     List<Integer> classIds, Integer uploaderId, String uploaderName) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("文件名无效");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("不支持的文件类型: " + extension);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小不能超过100MB");
        }

        if (folderId != null && folderId > 0) {
            RepositoryFolder folder = folderMapper.selectById(folderId);
            if (folder == null) {
                throw new RuntimeException("目录不存在");
            }
        }

        String baseName = originalFilename.contains(".") ? originalFilename.substring(0, originalFilename.lastIndexOf(".")) : originalFilename;
        String newFilename = generateUniqueFileName(folderId, baseName, extension);
        String storedName = UUID.randomUUID().toString() + "." + extension;
        File dest = new File(uploadPath + storedName);
        file.transferTo(dest);

        RepositoryFile repoFile = new RepositoryFile();
        repoFile.setName(newFilename);
        repoFile.setOriginalName(originalFilename);
        repoFile.setFileType(getFileType(extension));
        repoFile.setFileSize(file.getSize());
        repoFile.setFilePath("/uploads/" + storedName);
        repoFile.setFolderId(folderId != null ? folderId : 0);
        repoFile.setVisibilityType(visibilityType != null ? visibilityType : "INHERIT");
        repoFile.setUploaderId(uploaderId);
        repoFile.setUploaderName(uploaderName);
        repoFile.setDownloadCount(0);
        fileMapper.insert(repoFile);

        if ("CLASSES".equals(repoFile.getVisibilityType()) && classIds != null && !classIds.isEmpty()) {
            for (Integer classId : classIds) {
                RepositoryFileClass fc = new RepositoryFileClass();
                fc.setFileId(repoFile.getId());
                fc.setClassId(classId);
                fileClassMapper.insert(fc);
            }
        }

        return repoFile;
    }

    private String generateUniqueFileName(Integer folderId, String baseName, String extension) {
        String name = baseName + "." + extension;
        int counter = 1;
        while (true) {
            LambdaQueryWrapper<RepositoryFile> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RepositoryFile::getFolderId, folderId != null ? folderId : 0)
                    .eq(RepositoryFile::getName, name);
            if (fileMapper.selectCount(wrapper) == 0) {
                return name;
            }
            name = baseName + "(" + counter + ")." + extension;
            counter++;
        }
    }

    private String getFileType(String extension) {
        if (Arrays.asList("pdf").contains(extension)) return "pdf";
        if (Arrays.asList("doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md").contains(extension)) return "doc";
        if (Arrays.asList("zip", "rar", "7z", "tar", "gz").contains(extension)) return "zip";
        if (Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp").contains(extension)) return "image";
        if (Arrays.asList("mp4", "avi", "mov", "wmv", "flv").contains(extension)) return "video";
        if (Arrays.asList("mp3", "wav", "flac").contains(extension)) return "audio";
        return "other";
    }

    @Transactional
    public boolean moveFile(Integer fileId, Integer targetFolderId) {
        RepositoryFile file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }

        if (targetFolderId != null && targetFolderId > 0) {
            RepositoryFolder targetFolder = folderMapper.selectById(targetFolderId);
            if (targetFolder == null) {
                throw new RuntimeException("目标目录不存在");
            }
        }

        String baseName = file.getName().contains(".") ? file.getName().substring(0, file.getName().lastIndexOf(".")) : file.getName();
        String extension = file.getName().contains(".") ? file.getName().substring(file.getName().lastIndexOf(".") + 1) : "";
        String newName = generateUniqueFileName(targetFolderId, baseName, extension);

        file.setFolderId(targetFolderId != null ? targetFolderId : 0);
        file.setName(newName);
        fileMapper.updateById(file);

        return true;
    }

    @Transactional
    public boolean moveFolder(Integer sourceId, Integer targetParentId) {
        RepositoryFolder source = folderMapper.selectById(sourceId);
        if (source == null) {
            throw new RuntimeException("源目录不存在");
        }
        if (source.getParentId() == targetParentId) {
            return true;
        }

        if (targetParentId != null && targetParentId > 0) {
            RepositoryFolder target = folderMapper.selectById(targetParentId);
            if (target == null) {
                throw new RuntimeException("目标目录不存在");
            }
            if (target.getPath().startsWith(source.getPath())) {
                throw new RuntimeException("不能将目录移动到其子目录下");
            }
        }

        LambdaQueryWrapper<RepositoryFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepositoryFolder::getParentId, targetParentId != null ? targetParentId : 0)
                .eq(RepositoryFolder::getName, source.getName());
        if (folderMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("目标目录下已存在同名文件夹");
        }

        String oldPath = source.getPath();
        String newPath;
        int newDepth;
        if (targetParentId == null || targetParentId == 0) {
            newPath = "/" + source.getName() + "/";
            newDepth = 1;
        } else {
            RepositoryFolder target = folderMapper.selectById(targetParentId);
            newPath = target.getPath() + source.getName() + "/";
            newDepth = target.getDepth() + 1;
        }

        source.setParentId(targetParentId != null ? targetParentId : 0);
        source.setPath(newPath);
        source.setDepth(newDepth);
        folderMapper.updateById(source);

        updateChildPathsAndDepth(source.getId(), oldPath, newPath, newDepth);
        return true;
    }

    private void updateChildPathsAndDepth(Integer parentId, String oldPath, String newPath, int parentDepth) {
        List<RepositoryFolder> children = folderMapper.selectList(
                new LambdaQueryWrapper<RepositoryFolder>().eq(RepositoryFolder::getParentId, parentId)
        );
        for (RepositoryFolder child : children) {
            String childOldPath = child.getPath();
            String childNewPath = childOldPath.replace(oldPath, newPath);
            int childNewDepth = parentDepth + 1;
            child.setPath(childNewPath);
            child.setDepth(childNewDepth);
            folderMapper.updateById(child);
            updateChildPathsAndDepth(child.getId(), oldPath, newPath, childNewDepth);
        }
    }

    @Transactional
    public boolean renameFile(Integer id, String newName) {
        RepositoryFile file = fileMapper.selectById(id);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }

        String extension = "";
        String baseName = newName;
        if (newName.contains(".")) {
            extension = newName.substring(newName.lastIndexOf(".") + 1);
            baseName = newName.substring(0, newName.lastIndexOf("."));
        }

        String finalName = generateUniqueFileName(file.getFolderId(), baseName, extension);

        LambdaQueryWrapper<RepositoryFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepositoryFile::getFolderId, file.getFolderId())
                .eq(RepositoryFile::getName, finalName)
                .ne(RepositoryFile::getId, id);
        if (fileMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("同名文件已存在");
        }

        file.setName(finalName);
        fileMapper.updateById(file);
        return true;
    }

    @Transactional
    public boolean deleteFile(Integer id) {
        RepositoryFile file = fileMapper.selectById(id);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }
        deletePhysicalFile(file.getFilePath());
        fileMapper.deleteById(id);
        return true;
    }

    @Transactional
    public boolean setFolderVisibility(Integer folderId, VisibilityConfig config) {
        RepositoryFolder folder = folderMapper.selectById(folderId);
        if (folder == null) {
            throw new RuntimeException("目录不存在");
        }

        folder.setVisibilityType(config.getVisibilityType());
        folderMapper.updateById(folder);

        folderClassMapper.delete(new LambdaQueryWrapper<RepositoryFolderClass>()
                .eq(RepositoryFolderClass::getFolderId, folderId));

        if ("CLASSES".equals(config.getVisibilityType()) && config.getClassIds() != null) {
            for (Integer classId : config.getClassIds()) {
                RepositoryFolderClass fc = new RepositoryFolderClass();
                fc.setFolderId(folderId);
                fc.setClassId(classId);
                folderClassMapper.insert(fc);
            }
        }

        return true;
    }

    @Transactional
    public boolean setFileVisibility(Integer fileId, VisibilityConfig config) {
        RepositoryFile file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }

        file.setVisibilityType(config.getVisibilityType());
        fileMapper.updateById(file);

        fileClassMapper.delete(new LambdaQueryWrapper<RepositoryFileClass>()
                .eq(RepositoryFileClass::getFileId, fileId));

        if ("CLASSES".equals(config.getVisibilityType()) && config.getClassIds() != null) {
            for (Integer classId : config.getClassIds()) {
                RepositoryFileClass fc = new RepositoryFileClass();
                fc.setFileId(fileId);
                fc.setClassId(classId);
                fileClassMapper.insert(fc);
            }
        }

        return true;
    }

    public List<RepositoryFileDto> getFilesByFolder(Integer folderId, Integer studentId, String keyword, String fileType) {
        LambdaQueryWrapper<RepositoryFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepositoryFile::getFolderId, folderId != null ? folderId : 0);

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(RepositoryFile::getName, keyword.trim());
        }
        if (fileType != null && !fileType.trim().isEmpty() && !"all".equals(fileType)) {
            wrapper.eq(RepositoryFile::getFileType, fileType.trim());
        }

        List<RepositoryFile> files = fileMapper.selectList(wrapper);
        Map<Integer, List<Integer>> fileClassMap = getFileClassMap();
        Map<Integer, List<Integer>> folderClassMap = getFolderClassMap();
        Set<Integer> pinnedIds = studentId != null ? new HashSet<>(filePinMapper.getPinnedFileIds(studentId)) : new HashSet<>();

        List<RepositoryFileDto> result = new ArrayList<>();
        Integer userClassId = null;
        if (studentId != null) {
            User user = userMapper.selectById(studentId);
            if (user != null) {
                userClassId = user.getClassId();
            }
        }

        for (RepositoryFile file : files) {
            boolean canAccess = true;
            if (studentId != null && userClassId != null) {
                canAccess = checkFilePermission(file, userClassId, folderId, fileClassMap, folderClassMap);
            }
            if (canAccess) {
                RepositoryFileDto dto = convertToDto(file, fileClassMap, pinnedIds);
                result.add(dto);
            }
        }

        result.sort((a, b) -> {
            boolean aPinned = Boolean.TRUE.equals(a.getPinned());
            boolean bPinned = Boolean.TRUE.equals(b.getPinned());
            if (aPinned != bPinned) return aPinned ? -1 : 1;
            return a.getName().compareTo(b.getName());
        });

        return result;
    }

    private boolean checkFilePermission(RepositoryFile file, Integer classId, Integer folderId,
                                        Map<Integer, List<Integer>> fileClassMap,
                                        Map<Integer, List<Integer>> folderClassMap) {
        String visibility = file.getVisibilityType();
        if ("ALL".equals(visibility)) {
            return true;
        }
        if ("CLASSES".equals(visibility)) {
            List<Integer> classIds = fileClassMap.get(file.getId());
            return classIds != null && classIds.contains(classId);
        }
        if ("INHERIT".equals(visibility)) {
            if (folderId == null || folderId == 0) {
                return true;
            }
            List<Integer> classIds = folderClassMap.get(folderId);
            if (classIds == null || classIds.isEmpty()) {
                return true;
            }
            return classIds.contains(classId);
        }
        return true;
    }

    private RepositoryFileDto convertToDto(RepositoryFile file, Map<Integer, List<Integer>> classMap, Set<Integer> pinnedIds) {
        RepositoryFileDto dto = new RepositoryFileDto();
        dto.setId(file.getId());
        dto.setName(file.getName());
        dto.setOriginalName(file.getOriginalName());
        dto.setFileType(file.getFileType());
        dto.setFileSize(file.getFileSize());
        dto.setFilePath(file.getFilePath());
        dto.setFolderId(file.getFolderId());
        dto.setVisibilityType(file.getVisibilityType());
        dto.setClassIds(classMap.get(file.getId()));
        dto.setUploaderName(file.getUploaderName());
        dto.setDownloadCount(file.getDownloadCount());
        dto.setCreateTime(file.getCreateTime() != null ? file.getCreateTime().format(FORMATTER) : null);
        dto.setPinned(pinnedIds.contains(file.getId()));
        dto.setFileSizeDisplay(formatFileSize(file.getFileSize()));
        return dto;
    }

    private String formatFileSize(Long size) {
        if (size == null) return "0B";
        if (size < 1024) return size + "B";
        if (size < 1024 * 1024) return String.format("%.1fKB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1fMB", size / (1024.0 * 1024));
        return String.format("%.1fGB", size / (1024.0 * 1024 * 1024));
    }

    public RepositoryFileDto getFileForDownload(Integer id) {
        RepositoryFile file = fileMapper.selectById(id);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }
        fileMapper.incrementDownloadCount(id);
        Map<Integer, List<Integer>> classMap = getFileClassMap();
        return convertToDto(file, classMap, new HashSet<>());
    }

    @Transactional
    public boolean togglePin(Integer fileId, Integer studentId) {
        LambdaQueryWrapper<RepositoryFilePin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepositoryFilePin::getFileId, fileId)
                .eq(RepositoryFilePin::getStudentId, studentId);
        RepositoryFilePin existing = filePinMapper.selectOne(wrapper);

        if (existing != null) {
            filePinMapper.deleteById(existing.getId());
            return false;
        } else {
            RepositoryFilePin pin = new RepositoryFilePin();
            pin.setFileId(fileId);
            pin.setStudentId(studentId);
            filePinMapper.insert(pin);
            return true;
        }
    }

    public List<RepositoryFileDto> getPinnedFiles(Integer studentId) {
        List<Integer> pinnedIds = filePinMapper.getPinnedFileIds(studentId);
        if (pinnedIds.isEmpty()) {
            return new ArrayList<>();
        }

        User student = userMapper.selectById(studentId);
        Integer classId = student != null ? student.getClassId() : null;

        List<RepositoryFile> files = fileMapper.selectBatchIds(pinnedIds);
        Map<Integer, List<Integer>> fileClassMap = getFileClassMap();
        Map<Integer, List<Integer>> folderClassMap = getFolderClassMap();
        Set<Integer> pinnedSet = new HashSet<>(pinnedIds);

        List<RepositoryFileDto> result = new ArrayList<>();
        for (RepositoryFile file : files) {
            boolean canAccess = true;
            if (classId != null) {
                canAccess = checkFilePermission(file, classId, file.getFolderId(), fileClassMap, folderClassMap);
            }
            if (canAccess) {
                RepositoryFileDto dto = convertToDto(file, fileClassMap, pinnedSet);
                result.add(dto);
            }
        }

        result.sort((a, b) -> a.getName().compareTo(b.getName()));
        return result;
    }

    public Map<String, Object> getAllowedConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("maxSize", MAX_FILE_SIZE);
        config.put("maxSizeDisplay", formatFileSize(MAX_FILE_SIZE));
        config.put("allowedExtensions", ALLOWED_EXTENSIONS);
        return config;
    }
}
