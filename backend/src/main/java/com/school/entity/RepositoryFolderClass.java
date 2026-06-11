package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("repository_folder_class")
public class RepositoryFolderClass {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer folderId;
    private Integer classId;
    private LocalDateTime createTime;
}
