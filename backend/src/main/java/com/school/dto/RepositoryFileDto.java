package com.school.dto;

import lombok.Data;
import java.util.List;

@Data
public class RepositoryFileDto {
    private Integer id;
    private String name;
    private String originalName;
    private String fileType;
    private Long fileSize;
    private String filePath;
    private Integer folderId;
    private String visibilityType;
    private List<Integer> classIds;
    private String uploaderName;
    private Integer downloadCount;
    private String createTime;
    private Boolean pinned;
    private String fileSizeDisplay;
}
