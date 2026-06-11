package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.entity.RepositoryFolder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RepositoryFolderMapper extends BaseMapper<RepositoryFolder> {
    @Select("SELECT class_id FROM repository_folder_class WHERE folder_id = #{folderId}")
    List<Integer> getClassIdsByFolderId(@Param("folderId") Integer folderId);

    @Select("SELECT * FROM repository_folder WHERE path LIKE CONCAT(#{path}, '%')")
    List<RepositoryFolder> getChildrenByPath(@Param("path") String path);
}
