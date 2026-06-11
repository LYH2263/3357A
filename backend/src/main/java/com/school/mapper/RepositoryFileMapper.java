package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.entity.RepositoryFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RepositoryFileMapper extends BaseMapper<RepositoryFile> {
    @Select("SELECT class_id FROM repository_file_class WHERE file_id = #{fileId}")
    List<Integer> getClassIdsByFileId(@Param("fileId") Integer fileId);

    @Update("UPDATE repository_file SET download_count = download_count + 1 WHERE id = #{fileId}")
    void incrementDownloadCount(@Param("fileId") Integer fileId);

    @Select("SELECT * FROM repository_file WHERE folder_id = #{folderId} AND name = #{name} AND id != #{excludeId}")
    List<RepositoryFile> findDuplicateName(@Param("folderId") Integer folderId, @Param("name") String name, @Param("excludeId") Integer excludeId);
}
