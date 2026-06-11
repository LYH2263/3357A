package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.entity.CalendarEvent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CalendarEventMapper extends BaseMapper<CalendarEvent> {
}
