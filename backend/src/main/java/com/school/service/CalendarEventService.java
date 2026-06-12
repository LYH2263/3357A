package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.CalendarEvent;
import com.school.entity.CalendarEventClass;
import com.school.mapper.CalendarEventClassMapper;
import com.school.mapper.CalendarEventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CalendarEventService extends ServiceImpl<CalendarEventMapper, CalendarEvent> {

    @Autowired
    private CalendarEventClassMapper eventClassMapper;

    @Autowired
    private ClassesService classesService;

    public List<Map<String, Object>> listAllWithClasses() {
        archivePastEvents();
        List<CalendarEvent> events = this.list(new LambdaQueryWrapper<CalendarEvent>()
                .eq(CalendarEvent::getIsArchived, 0)
                .orderByAsc(CalendarEvent::getStartTime));
        return enrichWithClasses(events);
    }

    public List<Map<String, Object>> listByTimeRange(LocalDateTime start, LocalDateTime end) {
        archivePastEvents();
        List<CalendarEvent> events = this.list(new LambdaQueryWrapper<CalendarEvent>()
                .eq(CalendarEvent::getIsArchived, 0)
                .le(CalendarEvent::getStartTime, end)
                .ge(CalendarEvent::getEndTime, start)
                .orderByAsc(CalendarEvent::getStartTime));
        return enrichWithClasses(events);
    }

    public List<Map<String, Object>> listByTimeRangeAndType(LocalDateTime start, LocalDateTime end, String eventType) {
        archivePastEvents();
        LambdaQueryWrapper<CalendarEvent> wrapper = new LambdaQueryWrapper<CalendarEvent>()
                .eq(CalendarEvent::getIsArchived, 0)
                .le(CalendarEvent::getStartTime, end)
                .ge(CalendarEvent::getEndTime, start);
        if (eventType != null && !eventType.isEmpty()) {
            wrapper.eq(CalendarEvent::getEventType, eventType);
        }
        wrapper.orderByAsc(CalendarEvent::getStartTime);
        List<CalendarEvent> events = this.list(wrapper);
        return enrichWithClasses(events);
    }

    public List<Map<String, Object>> listByClassId(Integer classId, LocalDateTime start, LocalDateTime end) {
        archivePastEvents();
        List<CalendarEventClass> eventClasses = eventClassMapper.selectList(
                new LambdaQueryWrapper<CalendarEventClass>()
                        .eq(CalendarEventClass::getClassId, classId));
        if (eventClasses.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> eventIds = eventClasses.stream()
                .map(CalendarEventClass::getEventId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<CalendarEvent> wrapper = new LambdaQueryWrapper<CalendarEvent>()
                .eq(CalendarEvent::getIsArchived, 0)
                .in(CalendarEvent::getId, eventIds)
                .le(CalendarEvent::getStartTime, end)
                .ge(CalendarEvent::getEndTime, start)
                .orderByAsc(CalendarEvent::getStartTime);
        List<CalendarEvent> events = this.list(wrapper);
        return enrichWithClasses(events);
    }

    public List<Map<String, Object>> listUpcomingByClassId(Integer classId, Integer limit) {
        archivePastEvents();
        List<CalendarEventClass> eventClasses = eventClassMapper.selectList(
                new LambdaQueryWrapper<CalendarEventClass>()
                        .eq(CalendarEventClass::getClassId, classId));
        if (eventClasses.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> eventIds = eventClasses.stream()
                .map(CalendarEventClass::getEventId)
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<CalendarEvent> wrapper = new LambdaQueryWrapper<CalendarEvent>()
                .in(CalendarEvent::getId, eventIds)
                .gt(CalendarEvent::getEndTime, now)
                .eq(CalendarEvent::getIsArchived, 0)
                .orderByAsc(CalendarEvent::getStartTime);
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }
        List<CalendarEvent> events = this.list(wrapper);
        return enrichWithClasses(events);
    }

    @Transactional
    public CalendarEvent createEvent(CalendarEvent event, List<Integer> classIds) {
        validateEvent(event);
        if (classIds == null || classIds.isEmpty()) {
            throw new IllegalArgumentException("请至少选择一个面向班级");
        }
        event.setIsArchived(0);
        event.setCreateTime(LocalDateTime.now());
        this.save(event);

        for (Integer classId : classIds) {
            CalendarEventClass ec = new CalendarEventClass();
            ec.setEventId(event.getId());
            ec.setClassId(classId);
            ec.setCreateTime(LocalDateTime.now());
            eventClassMapper.insert(ec);
        }
        return event;
    }

    @Transactional
    public CalendarEvent updateEvent(CalendarEvent event, List<Integer> classIds) {
        validateEvent(event);
        CalendarEvent existing = this.getById(event.getId());
        if (existing == null) {
            throw new IllegalArgumentException("事件不存在");
        }
        event.setUpdateTime(LocalDateTime.now());
        this.updateById(event);

        if (classIds != null) {
            eventClassMapper.delete(new LambdaQueryWrapper<CalendarEventClass>()
                    .eq(CalendarEventClass::getEventId, event.getId()));
            for (Integer classId : classIds) {
                CalendarEventClass ec = new CalendarEventClass();
                ec.setEventId(event.getId());
                ec.setClassId(classId);
                ec.setCreateTime(LocalDateTime.now());
                eventClassMapper.insert(ec);
            }
        }
        return event;
    }

    @Transactional
    public boolean deleteEvent(Integer id) {
        eventClassMapper.delete(new LambdaQueryWrapper<CalendarEventClass>()
                .eq(CalendarEventClass::getEventId, id));
        return this.removeById(id);
    }

    public int archivePastEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<CalendarEvent> pastEvents = this.list(new LambdaQueryWrapper<CalendarEvent>()
                .lt(CalendarEvent::getEndTime, now)
                .eq(CalendarEvent::getIsArchived, 0));
        int count = 0;
        for (CalendarEvent event : pastEvents) {
            event.setIsArchived(1);
            this.updateById(event);
            count++;
        }
        return count;
    }

    public String exportICal(Integer classId) {
        archivePastEvents();
        List<CalendarEventClass> eventClasses = eventClassMapper.selectList(
                new LambdaQueryWrapper<CalendarEventClass>()
                        .eq(CalendarEventClass::getClassId, classId));
        List<Integer> eventIds = eventClasses.stream()
                .map(CalendarEventClass::getEventId)
                .collect(Collectors.toList());

        List<CalendarEvent> events = eventIds.isEmpty() ? Collections.emptyList() :
                this.list(new LambdaQueryWrapper<CalendarEvent>()
                        .in(CalendarEvent::getId, eventIds)
                        .eq(CalendarEvent::getIsArchived, 0)
                        .orderByAsc(CalendarEvent::getStartTime));

        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//SchoolSystem//Calendar//CN\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        for (CalendarEvent event : events) {
            sb.append("BEGIN:VEVENT\r\n");
            sb.append("DTSTART:").append(event.getStartTime().format(dtf)).append("\r\n");
            sb.append("DTEND:").append(event.getEndTime().format(dtf)).append("\r\n");
            sb.append("SUMMARY:").append(escapeIcal(event.getTitle())).append("\r\n");
            if (event.getLocation() != null && !event.getLocation().isEmpty()) {
                sb.append("LOCATION:").append(escapeIcal(event.getLocation())).append("\r\n");
            }
            if (event.getRemark() != null && !event.getRemark().isEmpty()) {
                sb.append("DESCRIPTION:").append(escapeIcal(event.getRemark())).append("\r\n");
            }
            sb.append("UID:").append(event.getId()).append("@school-system\r\n");
            sb.append("END:VEVENT\r\n");
        }
        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    public Map<String, Object> getDetail(Integer id) {
        CalendarEvent event = this.getById(id);
        if (event == null) {
            throw new IllegalArgumentException("事件不存在");
        }
        List<CalendarEventClass> eventClasses = eventClassMapper.selectList(
                new LambdaQueryWrapper<CalendarEventClass>()
                        .eq(CalendarEventClass::getEventId, id));
        List<Integer> classIds = eventClasses.stream()
                .map(CalendarEventClass::getClassId)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("event", event);
        result.put("classIds", classIds);
        return result;
    }

    private void validateEvent(CalendarEvent event) {
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("事件标题不能为空");
        }
        if (event.getStartTime() == null || event.getEndTime() == null) {
            throw new IllegalArgumentException("起止时间不能为空");
        }
        if (event.getEndTime().isBefore(event.getStartTime()) || event.getEndTime().isEqual(event.getStartTime())) {
            throw new IllegalArgumentException("结束时间必须晚于开始时间");
        }
        if (event.getEventType() == null || event.getEventType().trim().isEmpty()) {
            throw new IllegalArgumentException("事件类型不能为空");
        }
        Set<String> validTypes = new HashSet<>(Arrays.asList("exam", "lecture", "experiment", "activity"));
        if (!validTypes.contains(event.getEventType())) {
            throw new IllegalArgumentException("事件类型无效，可选：exam/lecture/experiment/activity");
        }
    }

    private List<Map<String, Object>> enrichWithClasses(List<CalendarEvent> events) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (CalendarEvent event : events) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", event.getId());
            map.put("title", event.getTitle());
            map.put("eventType", event.getEventType());
            map.put("startTime", event.getStartTime());
            map.put("endTime", event.getEndTime());
            map.put("location", event.getLocation());
            map.put("remark", event.getRemark());
            map.put("creatorId", event.getCreatorId());
            map.put("creatorName", event.getCreatorName());
            map.put("isArchived", event.getIsArchived());
            map.put("createTime", event.getCreateTime());

            List<CalendarEventClass> eventClasses = eventClassMapper.selectList(
                    new LambdaQueryWrapper<CalendarEventClass>()
                            .eq(CalendarEventClass::getEventId, event.getId()));
            List<Integer> classIds = eventClasses.stream()
                    .map(CalendarEventClass::getClassId)
                    .collect(Collectors.toList());

            List<String> classNames = new ArrayList<>();
            for (Integer cid : classIds) {
                try {
                    classNames.add(classesService.getById(cid).getCname());
                } catch (Exception ignored) {
                }
            }
            map.put("classIds", classIds);
            map.put("classNames", classNames);
            result.add(map);
        }
        return result;
    }

    private String escapeIcal(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
