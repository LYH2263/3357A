package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("class_album_image")
public class ClassAlbumImage {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer albumId;
    private String imagePath;
    private String imageName;
    private Long imageSize;
    private Integer sortOrder;
    private Integer isCover;
    private Integer uploaderId;
    private String uploaderName;
    private LocalDateTime createTime;
}
