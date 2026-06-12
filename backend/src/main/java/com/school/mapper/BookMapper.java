package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BookMapper extends BaseMapper<Book> {

    @Update("UPDATE book SET available_count = available_count - 1, version = version + 1, borrow_count = borrow_count + 1 " +
            "WHERE id = #{bookId} AND version = #{version} AND available_count > 0")
    int decreaseAvailableCount(@Param("bookId") Integer bookId, @Param("version") Integer version);

    @Update("UPDATE book SET available_count = available_count + 1, version = version + 1 " +
            "WHERE id = #{bookId} AND version = #{version} AND available_count < total_count")
    int increaseAvailableCount(@Param("bookId") Integer bookId, @Param("version") Integer version);

    @Update("UPDATE book SET available_count = available_count + 1, version = version + 1 " +
            "WHERE id = #{bookId} AND available_count < total_count")
    int increaseAvailableCountNoVersion(@Param("bookId") Integer bookId);
}
