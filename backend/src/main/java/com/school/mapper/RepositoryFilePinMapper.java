package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.entity.RepositoryFilePin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RepositoryFilePinMapper extends BaseMapper<RepositoryFilePin> {
    @Select("SELECT file_id FROM repository_file_pin WHERE student_id = #{studentId}")
    List<Integer> getPinnedFileIds(@Param("studentId") Integer studentId);
}
