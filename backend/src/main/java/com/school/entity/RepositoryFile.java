package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("repository_file")
public class RepositoryFile {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String originalName;
    private String fileType;
    private Long fileSize;
    private String filePath;
    private Integer folderId;
    private String visibilityType;
    private Integer uploaderId;
    private String uploaderName;
    private Integer downloadCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
