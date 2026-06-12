package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.Book;
import com.school.mapper.BookMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class BookService extends ServiceImpl<BookMapper, Book> {

    public Page<Book> searchBooks(String keyword, String category, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Book::getTitle, keyword)
                    .or().like(Book::getAuthor, keyword)
                    .or().like(Book::getIsbn, keyword));
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Book::getCategory, category);
        }
        wrapper.orderByDesc(Book::getBorrowCount);
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    public List<String> getAllCategories() {
        return this.listObjs(new LambdaQueryWrapper<Book>()
                .select(Book::getCategory)
                .isNotNull(Book::getCategory)
                .ne(Book::getCategory, "")
                .groupBy(Book::getCategory)
                .orderByAsc(Book::getCategory), Object::toString);
    }

    @Transactional
    public boolean saveBook(Book book) {
        if (book.getId() == null) {
            if (book.getTotalCount() != null && book.getAvailableCount() == null) {
                book.setAvailableCount(book.getTotalCount());
            }
            if (book.getBorrowCount() == null) {
                book.setBorrowCount(0);
            }
            if (book.getVersion() == null) {
                book.setVersion(0);
            }
            return this.save(book);
        } else {
            Book existing = this.getById(book.getId());
            if (existing == null) {
                throw new IllegalArgumentException("书目不存在");
            }
            if (book.getTotalCount() != null) {
                int diff = book.getTotalCount() - existing.getTotalCount();
                int newAvailable = existing.getAvailableCount() + diff;
                if (newAvailable < 0) {
                    throw new IllegalArgumentException("馆藏总数不能小于已借出数量");
                }
                book.setAvailableCount(newAvailable);
            } else {
                book.setAvailableCount(existing.getAvailableCount());
            }
            book.setBorrowCount(existing.getBorrowCount());
            book.setVersion(existing.getVersion());
            book.setCreateTime(existing.getCreateTime());
            return this.updateById(book);
        }
    }

    @Transactional
    public boolean updateTotalCount(Integer bookId, Integer newTotalCount) {
        Book book = this.getById(bookId);
        if (book == null) {
            throw new IllegalArgumentException("书目不存在");
        }
        int diff = newTotalCount - book.getTotalCount();
        if (book.getAvailableCount() + diff < 0) {
            throw new IllegalArgumentException("馆藏总数不能小于已借出数量");
        }
        book.setTotalCount(newTotalCount);
        book.setAvailableCount(book.getAvailableCount() + diff);
        return this.updateById(book);
    }

    @Transactional
    public boolean decreaseStock(Integer bookId, Integer version) {
        int rows = baseMapper.decreaseAvailableCount(bookId, version);
        return rows > 0;
    }

    @Transactional
    public boolean increaseStock(Integer bookId, Integer version) {
        int rows = baseMapper.increaseAvailableCount(bookId, version);
        return rows > 0;
    }

    @Transactional
    public boolean increaseStockNoVersion(Integer bookId) {
        int rows = baseMapper.increaseAvailableCountNoVersion(bookId);
        return rows > 0;
    }
}
