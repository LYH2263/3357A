package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("class_album_comment")
public class ClassAlbumComment {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer albumId;
    private Integer userId;
    private String userName;
    private String userType;
    private String content;
    private String imagePath;
    private LocalDateTime createTime;
}
