package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.entity.Score;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ScoreMapper extends BaseMapper<Score> {

    @Update("UPDATE score SET class_rank = (" +
            "SELECT rk FROM (" +
            "SELECT id, RANK() OVER (ORDER BY total_score DESC) as rk " +
            "FROM score WHERE course_id = #{courseId} AND class_id = #{classId} AND status = 'normal' AND total_score IS NOT NULL" +
            ") t WHERE t.id = score.id) " +
            "WHERE course_id = #{courseId} AND class_id = #{classId} AND status = 'normal' AND total_score IS NOT NULL")
    int updateClassRank(@Param("courseId") Integer courseId, @Param("classId") Integer classId);
}
