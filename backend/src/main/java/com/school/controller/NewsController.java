package com.school.controller;

import com.school.entity.News;
import com.school.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/news")
@CrossOrigin
public class NewsController {
    @Autowired private NewsService newsService;

    @GetMapping("/list") public List<News> list() { return newsService.list(); }
    @PostMapping("/save") public boolean save(@RequestBody News news) {
        if (news.getNewsdate() == null) news.setNewsdate(LocalDateTime.now());
        return newsService.saveOrUpdate(news);
    }
    @DeleteMapping("/delete/{id}") public boolean delete(@PathVariable Integer id) { return newsService.removeById(id); }
}
