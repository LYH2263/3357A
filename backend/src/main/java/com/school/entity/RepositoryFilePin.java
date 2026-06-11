package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("repository_file_pin")
public class RepositoryFilePin {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer fileId;
    private Integer studentId;
    private LocalDateTime createTime;
}
