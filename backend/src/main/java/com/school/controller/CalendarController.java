package com.school.controller;

import com.school.entity.CalendarEvent;
import com.school.service.CalendarEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
@CrossOrigin
public class CalendarController {

    @Autowired
    private CalendarEventService calendarEventService;

    @GetMapping("/event/list")
    public List<Map<String, Object>> listAll() {
        return calendarEventService.listAllWithClasses();
    }

    @GetMapping("/event/range")
    public List<Map<String, Object>> listByRange(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) String eventType) {
        LocalDateTime startTime = LocalDateTime.parse(start, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime endTime = LocalDateTime.parse(end, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        if (eventType != null && !eventType.isEmpty()) {
            return calendarEventService.listByTimeRangeAndType(startTime, endTime, eventType);
        }
        return calendarEventService.listByTimeRange(startTime, endTime);
    }

    @GetMapping("/event/class")
    public List<Map<String, Object>> listByClass(
            @RequestParam Integer classId,
            @RequestParam String start,
            @RequestParam String end) {
        LocalDateTime startTime = LocalDateTime.parse(start, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime endTime = LocalDateTime.parse(end, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return calendarEventService.listByClassId(classId, startTime, endTime);
    }

    @GetMapping("/event/upcoming")
    public List<Map<String, Object>> upcomingByClass(
            @RequestParam Integer classId,
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        return calendarEventService.listUpcomingByClassId(classId, limit);
    }

    @GetMapping("/event/detail")
    public Map<String, Object> detail(@RequestParam Integer id) {
        return calendarEventService.getDetail(id);
    }

    @PostMapping("/event/create")
    public CalendarEvent create(@RequestBody Map<String, Object> params) {
        CalendarEvent event = new CalendarEvent();
        event.setTitle((String) params.get("title"));
        event.setEventType((String) params.get("eventType"));

        String startTimeStr = (String) params.get("startTime");
        String endTimeStr = (String) params.get("endTime");
        event.setStartTime(LocalDateTime.parse(startTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        event.setEndTime(LocalDateTime.parse(endTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        event.setLocation((String) params.get("location"));
        event.setRemark((String) params.get("remark"));

        Object creatorId = params.get("creatorId");
        if (creatorId != null) {
            event.setCreatorId(((Number) creatorId).intValue());
        }
        event.setCreatorName((String) params.get("creatorName"));

        @SuppressWarnings("unchecked")
        List<Integer> classIds = (List<Integer>) params.get("classIds");

        return calendarEventService.createEvent(event, classIds);
    }

    @PostMapping("/event/update")
    public CalendarEvent update(@RequestBody Map<String, Object> params) {
        CalendarEvent event = new CalendarEvent();
        Object idObj = params.get("id");
        if (idObj != null) {
            event.setId(((Number) idObj).intValue());
        }
        event.setTitle((String) params.get("title"));
        event.setEventType((String) params.get("eventType"));

        String startTimeStr = (String) params.get("startTime");
        String endTimeStr = (String) params.get("endTime");
        if (startTimeStr != null) {
            event.setStartTime(LocalDateTime.parse(startTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (endTimeStr != null) {
            event.setEndTime(LocalDateTime.parse(endTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        event.setLocation((String) params.get("location"));
        event.setRemark((String) params.get("remark"));

        Object creatorId = params.get("creatorId");
        if (creatorId != null) {
            event.setCreatorId(((Number) creatorId).intValue());
        }
        event.setCreatorName((String) params.get("creatorName"));

        Object isArchived = params.get("isArchived");
        if (isArchived != null) {
            event.setIsArchived(((Number) isArchived).intValue());
        }

        @SuppressWarnings("unchecked")
        List<Integer> classIds = (List<Integer>) params.get("classIds");

        return calendarEventService.updateEvent(event, classIds);
    }

    @DeleteMapping("/event/delete/{id}")
    public boolean delete(@PathVariable Integer id) {
        return calendarEventService.deleteEvent(id);
    }

    @PostMapping("/event/archive")
    public Map<String, Object> archivePast() {
        int count = calendarEventService.archivePastEvents();
        return Map.of("archivedCount", count);
    }

    @GetMapping("/export/ical")
    public ResponseEntity<String> exportICal(@RequestParam Integer classId) {
        String icalContent = calendarEventService.exportICal(classId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=school-calendar.ics")
                .contentType(MediaType.parseMediaType("text/calendar; charset=utf-8"))
                .body(icalContent);
    }
}
