package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("experiment")
public class Experiment {
    @TableId(type = IdType.AUTO)
    private Integer eid;
    private String etitle;
    private String econtent;
    private String efile;
}
