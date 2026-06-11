package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("class_album")
public class ClassAlbum {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String description;
    private LocalDate activityDate;
    private Integer classId;
    private String className;
    private String coverImage;
    private Integer creatorId;
    private String creatorName;
    private String creatorType;
    private Integer likeCount;
    private Integer viewCount;
    private Integer commentCount;
    private Integer isFeatured;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
