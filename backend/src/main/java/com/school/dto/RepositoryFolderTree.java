package com.school.dto;

import lombok.Data;
import java.util.List;

@Data
public class RepositoryFolderTree {
    private Integer id;
    private String name;
    private Integer parentId;
    private String path;
    private Integer depth;
    private String visibilityType;
    private List<Integer> classIds;
    private String creatorName;
    private String createTime;
    private List<RepositoryFolderTree> children;
    private List<RepositoryFileDto> files;
}
