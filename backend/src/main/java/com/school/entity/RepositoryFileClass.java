package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("repository_file_class")
public class RepositoryFileClass {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer fileId;
    private Integer classId;
    private LocalDateTime createTime;
}
