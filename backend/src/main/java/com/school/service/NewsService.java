package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.News;
import com.school.mapper.NewsMapper;
import org.springframework.stereotype.Service;

@Service
public class NewsService extends ServiceImpl<NewsMapper, News> {
}
