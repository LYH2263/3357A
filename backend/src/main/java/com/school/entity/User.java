package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer uid;
    private String username;
    private String userpassword;
    private String usersex;
    private String userno;
    private String userdescript;
    private Integer classId;
    private String upic;
    private String youxiuok;
    private String checkedok;
    private String classname;
}
