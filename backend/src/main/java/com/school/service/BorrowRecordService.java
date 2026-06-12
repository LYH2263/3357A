package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.dto.BookHotStats;
import com.school.dto.BorrowRecordDto;
import com.school.entity.Book;
import com.school.entity.BorrowRecord;
import com.school.entity.User;
import com.school.mapper.BorrowRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BorrowRecordService extends ServiceImpl<BorrowRecordMapper, BorrowRecord> {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    private static final int BORROW_DAYS = 14;

    public int updateOverdueRecords() {
        return baseMapper.updateOverdueRecords(LocalDate.now());
    }

    @Transactional
    public Map<String, Object> borrowBook(Integer bookId, Integer studentId) {
        Map<String, Object> result = new HashMap<>();

        updateOverdueRecords();

        Book book = bookService.getById(bookId);
        if (book == null) {
            result.put("success", false);
            result.put("message", "书目不存在");
            return result;
        }

        if (book.getAvailableCount() <= 0) {
            result.put("success", false);
            result.put("message", "该书已借完");
            return result;
        }

        int activeCount = baseMapper.countActiveBorrowByBookAndStudent(bookId, studentId);
        if (activeCount > 0) {
            result.put("success", false);
            result.put("message", "您已借阅该书，尚未归还");
            return result;
        }

        boolean stockDecreased = bookService.decreaseStock(bookId, book.getVersion());
        if (!stockDecreased) {
            result.put("success", false);
            result.put("message", "借阅失败，请稍后重试");
            return result;
        }

        User student = userService.getById(studentId);
        if (student == null) {
            bookService.increaseStockNoVersion(bookId);
            result.put("success", false);
            result.put("message", "学生不存在");
            return result;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate dueDate = LocalDate.now().plusDays(BORROW_DAYS);

        BorrowRecord record = new BorrowRecord();
        record.setBookId(bookId);
        record.setBookTitle(book.getTitle());
        record.setStudentId(studentId);
        record.setStudentName(student.getUsername());
        record.setStudentNo(student.getUserno());
        record.setBorrowTime(now);
        record.setDueDate(dueDate);
        record.setStatus("borrowing");
        record.setIsOverdue(0);
        record.setRenewCount(0);

        try {
            this.save(record);
            result.put("success", true);
            result.put("message", "借阅成功");
            result.put("data", record);
        } catch (Exception e) {
            bookService.increaseStockNoVersion(bookId);
            result.put("success", false);
            result.put("message", "借阅失败：" + e.getMessage());
        }

        return result;
    }

    @Transactional
    public Map<String, Object> returnBook(Integer recordId) {
        Map<String, Object> result = new HashMap<>();

        BorrowRecord record = this.getById(recordId);
        if (record == null) {
            result.put("success", false);
            result.put("message", "借阅记录不存在");
            return result;
        }

        if ("returned".equals(record.getStatus())) {
            result.put("success", true);
            result.put("message", "该书已归还");
            result.put("data", record);
            return result;
        }

        if (!"borrowing".equals(record.getStatus()) && !"overdue".equals(record.getStatus())) {
            result.put("success", false);
            result.put("message", "该记录状态不支持归还操作");
            return result;
        }

        LocalDateTime returnTime = LocalDateTime.now();
        int updated = baseMapper.returnBook(recordId, returnTime);
        if (updated == 0) {
            result.put("success", false);
            result.put("message", "归还失败，请稍后重试");
            return result;
        }

        Book book = bookService.getById(record.getBookId());
        if (book != null) {
            bookService.increaseStockNoVersion(record.getBookId());
        }

        result.put("success", true);
        result.put("message", "归还成功");
        result.put("data", record);
        return result;
    }

    public List<BorrowRecordDto> getMyRecords(Integer studentId) {
        updateOverdueRecords();
        return baseMapper.findByStudentIdWithDetails(studentId);
    }

    public List<BorrowRecordDto> getAllRecords(String status, String keyword) {
        updateOverdueRecords();
        if (StringUtils.hasText(keyword)) {
            return baseMapper.searchRecords(keyword);
        }
        if (StringUtils.hasText(status) && !"all".equals(status)) {
            if ("overdue".equals(status)) {
                return baseMapper.findOverdueRecords();
            }
            return baseMapper.findByStatusWithDetails(status);
        }
        return baseMapper.findAllWithDetails();
    }

    public Page<BorrowRecordDto> getRecordsPage(String status, String keyword, Integer pageNum, Integer pageSize) {
        updateOverdueRecords();
        List<BorrowRecordDto> allRecords = getAllRecords(status, keyword);
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allRecords.size());
        List<BorrowRecordDto> records = allRecords.subList(start, end);

        Page<BorrowRecordDto> page = new Page<>(pageNum, pageSize);
        page.setRecords(records);
        page.setTotal(allRecords.size());
        return page;
    }

    public List<BorrowRecordDto> getRecordsByBookId(Integer bookId) {
        updateOverdueRecords();
        return baseMapper.findByBookIdWithDetails(bookId);
    }

    public List<BookHotStats> getHotBooks(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return baseMapper.findHotBooks(limit);
    }

    public Map<String, Object> getStatistics() {
        updateOverdueRecords();
        Map<String, Object> stats = new HashMap<>();

        long totalBooks = bookService.count();
        long totalBorrowed = this.count(new LambdaQueryWrapper<BorrowRecord>()
                .in(BorrowRecord::getStatus, "borrowing", "overdue"));
        long overdueCount = this.count(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getIsOverdue, 1));
        long totalRecords = this.count();

        stats.put("totalBooks", totalBooks);
        stats.put("totalBorrowed", totalBorrowed);
        stats.put("overdueCount", overdueCount);
        stats.put("totalRecords", totalRecords);

        List<BookHotStats> hotBooks = getHotBooks(10);
        stats.put("hotBooks", hotBooks);

        return stats;
    }

    @Transactional
    public Map<String, Object> renewBook(Integer recordId) {
        Map<String, Object> result = new HashMap<>();
        updateOverdueRecords();

        BorrowRecord record = this.getById(recordId);
        if (record == null) {
            result.put("success", false);
            result.put("message", "借阅记录不存在");
            return result;
        }

        if ("overdue".equals(record.getStatus())) {
            result.put("success", false);
            result.put("message", "逾期图书不能续借，请先归还");
            return result;
        }

        if (!"borrowing".equals(record.getStatus())) {
            result.put("success", false);
            result.put("message", "该记录状态不支持续借");
            return result;
        }

        if (record.getRenewCount() != null && record.getRenewCount() >= 1) {
            result.put("success", false);
            result.put("message", "每本书最多续借1次");
            return result;
        }

        record.setDueDate(record.getDueDate().plusDays(BORROW_DAYS));
        record.setRenewCount(record.getRenewCount() == null ? 1 : record.getRenewCount() + 1);
        this.updateById(record);

        result.put("success", true);
        result.put("message", "续借成功，应还日期已延长至 " + record.getDueDate());
        result.put("data", record);
        return result;
    }
}
