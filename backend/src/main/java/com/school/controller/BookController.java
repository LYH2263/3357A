package com.school.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.dto.BookHotStats;
import com.school.dto.BorrowRecordDto;
import com.school.entity.Book;
import com.school.service.BookService;
import com.school.service.BorrowRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/book")
@CrossOrigin
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private BorrowRecordService borrowRecordService;

    @Value("${spring.servlet.multipart.location:/app/uploads/}")
    private String uploadPath;

    @GetMapping("/list")
    public Map<String, Object> getBookList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            Page<Book> page = bookService.searchBooks(keyword, category, pageNum, pageSize);
            result.put("success", true);
            result.put("data", page);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getBookDetail(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Book book = bookService.getById(id);
            if (book == null) {
                result.put("success", false);
                result.put("message", "书目不存在");
                return result;
            }
            result.put("success", true);
            result.put("data", book);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/categories")
    public Map<String, Object> getCategories() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<String> categories = bookService.getAllCategories();
            result.put("success", true);
            result.put("data", categories);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/save")
    public Map<String, Object> saveBook(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Book book = new Book();
            if (params.get("id") != null) {
                book.setId(((Number) params.get("id")).intValue());
            }
            book.setTitle((String) params.get("title"));
            book.setAuthor((String) params.get("author"));
            book.setIsbn((String) params.get("isbn"));
            book.setCategory((String) params.get("category"));
            book.setDescription((String) params.get("description"));
            book.setCoverImage((String) params.get("coverImage"));

            if (params.get("totalCount") != null) {
                book.setTotalCount(((Number) params.get("totalCount")).intValue());
            }
            if (params.get("availableCount") != null) {
                book.setAvailableCount(((Number) params.get("availableCount")).intValue());
            }
            if (params.get("creatorId") != null) {
                book.setCreatorId(((Number) params.get("creatorId")).intValue());
            }
            book.setCreatorName((String) params.get("creatorName"));

            boolean success = bookService.saveBook(book);
            result.put("success", success);
            result.put("data", book);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/delete/{id}")
    public Map<String, Object> deleteBook(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<BorrowRecordDto> activeRecords = borrowRecordService.getRecordsByBookId(id);
            boolean hasActive = activeRecords.stream()
                    .anyMatch(r -> "borrowing".equals(r.getStatus()) || "overdue".equals(r.getStatus()));
            if (hasActive) {
                result.put("success", false);
                result.put("message", "该书仍有未归还的借阅记录，无法删除");
                return result;
            }
            boolean success = bookService.removeById(id);
            result.put("success", success);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/cover/upload")
    public Map<String, Object> uploadCover(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择文件");
                return result;
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";

            String newFilename = "book_cover_" + UUID.randomUUID().toString().replace("-", "") + extension;
            String filePath = uploadPath + newFilename;
            String relativePath = "/uploads/" + newFilename;

            File destFile = new File(filePath);
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            }
            file.transferTo(destFile);

            result.put("success", true);
            result.put("data", relativePath);
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "文件上传失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/borrow")
    public Map<String, Object> borrowBook(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer bookId = ((Number) params.get("bookId")).intValue();
            Integer studentId = ((Number) params.get("studentId")).intValue();
            result = borrowRecordService.borrowBook(bookId, studentId);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/return/{recordId}")
    public Map<String, Object> returnBook(@PathVariable Integer recordId) {
        Map<String, Object> result = new HashMap<>();
        try {
            result = borrowRecordService.returnBook(recordId);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/renew/{recordId}")
    public Map<String, Object> renewBook(@PathVariable Integer recordId) {
        Map<String, Object> result = new HashMap<>();
        try {
            result = borrowRecordService.renewBook(recordId);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/record/my")
    public Map<String, Object> getMyRecords(@RequestParam Integer studentId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<BorrowRecordDto> records = borrowRecordService.getMyRecords(studentId);
            result.put("success", true);
            result.put("data", records);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/record/list")
    public Map<String, Object> getRecordList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            Page<BorrowRecordDto> page = borrowRecordService.getRecordsPage(status, keyword, pageNum, pageSize);
            result.put("success", true);
            result.put("data", page);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/record/overdue")
    public Map<String, Object> getOverdueRecords() {
        Map<String, Object> result = new HashMap<>();
        try {
            borrowRecordService.updateOverdueRecords();
            List<BorrowRecordDto> records = borrowRecordService.getAllRecords("overdue", null);
            result.put("success", true);
            result.put("data", records);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/record/book/{bookId}")
    public Map<String, Object> getRecordsByBook(@PathVariable Integer bookId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<BorrowRecordDto> records = borrowRecordService.getRecordsByBookId(bookId);
            result.put("success", true);
            result.put("data", records);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/hot")
    public Map<String, Object> getHotBooks(@RequestParam(defaultValue = "10") Integer limit) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<BookHotStats> hotBooks = borrowRecordService.getHotBooks(limit);
            result.put("success", true);
            result.put("data", hotBooks);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> stats = borrowRecordService.getStatistics();
            result.put("success", true);
            result.put("data", stats);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/updateOverdue")
    public Map<String, Object> updateOverdue() {
        Map<String, Object> result = new HashMap<>();
        try {
            int count = borrowRecordService.updateOverdueRecords();
            result.put("success", true);
            result.put("data", count);
            result.put("message", "已更新 " + count + " 条逾期记录");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
