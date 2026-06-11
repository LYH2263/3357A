package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("interaction")
public class Interaction {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String comask;
    private LocalDateTime asktime;
    private String replname;
    private String comrepl;
    private LocalDateTime repltime;
}
