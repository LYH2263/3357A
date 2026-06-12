package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.dto.BookHotStats;
import com.school.dto.BorrowRecordDto;
import com.school.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {

    @Update("UPDATE borrow_record SET status = 'overdue', is_overdue = 1, update_time = NOW() " +
            "WHERE status = 'borrowing' AND due_date < #{today}")
    int updateOverdueRecords(@Param("today") LocalDate today);

    @Select("SELECT br.*, b.cover_image as bookCover, b.author, b.isbn, b.category, u.classname as className " +
            "FROM borrow_record br " +
            "LEFT JOIN book b ON br.book_id = b.id " +
            "LEFT JOIN user u ON br.student_id = u.uid " +
            "WHERE br.student_id = #{studentId} " +
            "ORDER BY br.borrow_time DESC")
    List<BorrowRecordDto> findByStudentIdWithDetails(@Param("studentId") Integer studentId);

    @Select("SELECT br.*, b.cover_image as bookCover, b.author, b.isbn, b.category, u.classname as className " +
            "FROM borrow_record br " +
            "LEFT JOIN book b ON br.book_id = b.id " +
            "LEFT JOIN user u ON br.student_id = u.uid " +
            "WHERE br.status = #{status} " +
            "ORDER BY br.borrow_time DESC")
    List<BorrowRecordDto> findByStatusWithDetails(@Param("status") String status);

    @Select("SELECT br.*, b.cover_image as bookCover, b.author, b.isbn, b.category, u.classname as className " +
            "FROM borrow_record br " +
            "LEFT JOIN book b ON br.book_id = b.id " +
            "LEFT JOIN user u ON br.student_id = u.uid " +
            "WHERE br.is_overdue = 1 " +
            "ORDER BY br.due_date ASC")
    List<BorrowRecordDto> findOverdueRecords();

    @Select("SELECT br.*, b.cover_image as bookCover, b.author, b.isbn, b.category, u.classname as className " +
            "FROM borrow_record br " +
            "LEFT JOIN book b ON br.book_id = b.id " +
            "LEFT JOIN user u ON br.student_id = u.uid " +
            "WHERE br.book_id = #{bookId} " +
            "ORDER BY br.borrow_time DESC")
    List<BorrowRecordDto> findByBookIdWithDetails(@Param("bookId") Integer bookId);

    @Select("SELECT br.*, b.cover_image as bookCover, b.author, b.isbn, b.category, u.classname as className " +
            "FROM borrow_record br " +
            "LEFT JOIN book b ON br.book_id = b.id " +
            "LEFT JOIN user u ON br.student_id = u.uid " +
            "ORDER BY br.borrow_time DESC")
    List<BorrowRecordDto> findAllWithDetails();

    @Select("SELECT b.id as bookId, b.title, b.author, b.category, b.borrow_count as borrowCount, " +
            "b.total_count as totalCount, b.available_count as availableCount, b.cover_image as coverImage " +
            "FROM book b " +
            "ORDER BY b.borrow_count DESC " +
            "LIMIT #{limit}")
    List<BookHotStats> findHotBooks(@Param("limit") Integer limit);

    @Select("SELECT COUNT(*) FROM borrow_record br " +
            "WHERE br.book_id = #{bookId} AND br.student_id = #{studentId} " +
            "AND br.status IN ('borrowing', 'overdue')")
    int countActiveBorrowByBookAndStudent(@Param("bookId") Integer bookId, @Param("studentId") Integer studentId);

    @Select("SELECT br.*, b.cover_image as bookCover, b.author, b.isbn, b.category, u.classname as className " +
            "FROM borrow_record br " +
            "LEFT JOIN book b ON br.book_id = b.id " +
            "LEFT JOIN user u ON br.student_id = u.uid " +
            "WHERE (br.student_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR br.student_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR b.title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR b.isbn LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY br.borrow_time DESC")
    List<BorrowRecordDto> searchRecords(@Param("keyword") String keyword);

    @Update("UPDATE borrow_record SET status = 'returned', return_time = #{returnTime}, is_overdue = 0, update_time = NOW() " +
            "WHERE id = #{id} AND status IN ('borrowing', 'overdue')")
    int returnBook(@Param("id") Integer id, @Param("returnTime") LocalDateTime returnTime);
}
