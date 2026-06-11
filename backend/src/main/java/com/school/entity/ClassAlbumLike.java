package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("class_album_like")
public class ClassAlbumLike {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer albumId;
    private Integer userId;
    private String userType;
    private LocalDateTime createTime;
}
