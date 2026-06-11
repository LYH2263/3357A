package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper extends BaseMapper<User> {

    @Select("<script>" +
            "SELECT checkedok AS name, COUNT(*) AS value FROM user " +
            "<where>" +
            "<if test='classIds != null and classIds.size() > 0'>AND class_id IN " +
            "<foreach item='cid' collection='classIds' open='(' separator=',' close=')'>#{cid}</foreach>" +
            "</if>" +
            "</where>" +
            " GROUP BY checkedok" +
            "</script>")
    List<Map<String, Object>> countStudentByAuditStatus(@Param("classIds") List<Integer> classIds);

    @Select("<script>" +
            "SELECT c.cid AS classId, c.cname AS className, " +
            "COUNT(u.uid) AS studentCount, " +
            "SUM(CASE WHEN u.youxiuok = '是' THEN 1 ELSE 0 END) AS excellentCount " +
            "FROM classes c LEFT JOIN user u ON c.cid = u.class_id " +
            "<where>" +
            "<if test='classIds != null and classIds.size() > 0'>AND c.cid IN " +
            "<foreach item='cid' collection='classIds' open='(' separator=',' close=')'>#{cid}</foreach>" +
            "</if>" +
            "</where>" +
            " GROUP BY c.cid, c.cname ORDER BY studentCount DESC" +
            "</script>")
    List<Map<String, Object>> countStudentByClass(@Param("classIds") List<Integer> classIds);

    @Select("<script>" +
            "SELECT " +
            "COUNT(*) AS totalQuestions, " +
            "SUM(CASE WHEN comrepl IS NOT NULL AND comrepl != '' THEN 1 ELSE 0 END) AS repliedCount, " +
            "SUM(CASE WHEN comrepl IS NULL OR comrepl = '' THEN 1 ELSE 0 END) AS unansweredCount " +
            "FROM interaction " +
            "<where>" +
            "<if test='startTime != null'>AND asktime &gt;= #{startTime}</if>" +
            "<if test='endTime != null'>AND asktime &lt;= #{endTime}</if>" +
            "</where>" +
            "</script>")
    Map<String, Object> countInteractionSummary(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Select("<script>" +
            "SELECT AVG(TIMESTAMPDIFF(MINUTE, asktime, repltime)) AS avgMinutes " +
            "FROM interaction " +
            "WHERE asktime IS NOT NULL AND repltime IS NOT NULL " +
            "<if test='startTime != null'>AND asktime &gt;= #{startTime}</if>" +
            "<if test='endTime != null'>AND asktime &lt;= #{endTime}</if>" +
            "</script>")
    Double getAvgResponseMinutes(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Select("<script>" +
            "SELECT DATE(CONVERT_TZ(asktime, '+00:00', #{tzOffset})) AS date, " +
            "COUNT(*) AS questionCount, " +
            "SUM(CASE WHEN comrepl IS NOT NULL AND comrepl != '' THEN 1 ELSE 0 END) AS replyCount " +
            "FROM interaction " +
            "<where>" +
            "<if test='startTime != null'>AND asktime &gt;= #{startTime}</if>" +
            "<if test='endTime != null'>AND asktime &lt;= #{endTime}</if>" +
            "</where>" +
            " GROUP BY DATE(CONVERT_TZ(asktime, '+00:00', #{tzOffset})) ORDER BY date ASC" +
            "</script>")
    List<Map<String, Object>> countInteractionDaily(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("tzOffset") String tzOffset);

    @Select("SELECT " +
            "COUNT(*) AS courseCount, " +
            "SUM(CASE WHEN efile IS NOT NULL AND efile != '' THEN 1 ELSE 0 END) AS courseWithFileCount " +
            "FROM course")
    Map<String, Object> countCourseStats();

    @Select("SELECT " +
            "COUNT(*) AS experimentCount, " +
            "SUM(CASE WHEN efile IS NOT NULL AND efile != '' THEN 1 ELSE 0 END) AS experimentWithFileCount " +
            "FROM experiment")
    Map<String, Object> countExperimentStats();

    @Select("<script>" +
            "SELECT COUNT(*) AS totalCount FROM news " +
            "<where>" +
            "<if test='startTime != null'>AND newsdate &gt;= #{startTime}</if>" +
            "<if test='endTime != null'>AND newsdate &lt;= #{endTime}</if>" +
            "</where>" +
            "</script>")
    Long countNewsTotal(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Select("<script>" +
            "SELECT DATE(CONVERT_TZ(newsdate, '+00:00', #{tzOffset})) AS date, " +
            "COUNT(*) AS count " +
            "FROM news " +
            "<where>" +
            "<if test='startTime != null'>AND newsdate &gt;= #{startTime}</if>" +
            "<if test='endTime != null'>AND newsdate &lt;= #{endTime}</if>" +
            "</where>" +
            " GROUP BY DATE(CONVERT_TZ(newsdate, '+00:00', #{tzOffset})) ORDER BY date ASC" +
            "</script>")
    List<Map<String, Object>> countNewsDaily(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("tzOffset") String tzOffset);
}
