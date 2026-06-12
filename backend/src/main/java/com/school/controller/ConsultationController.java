package com.school.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.entity.ConsultationBooking;
import com.school.entity.ConsultationSlot;
import com.school.entity.Teacher;
import com.school.entity.User;
import com.school.service.ConsultationBookingService;
import com.school.service.ConsultationSlotService;
import com.school.service.TeacherService;
import com.school.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultation")
@CrossOrigin
public class ConsultationController {

    @Autowired
    private ConsultationSlotService slotService;

    @Autowired
    private ConsultationBookingService bookingService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private UserService userService;

    @GetMapping("/teacher/slots")
    public List<ConsultationSlot> getTeacherSlots(@RequestParam Integer teacherId,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) String startDate,
                                                   @RequestParam(required = false) String endDate) {
        LambdaQueryWrapper<ConsultationSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationSlot::getTeacherId, teacherId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ConsultationSlot::getStatus, status);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(ConsultationSlot::getSlotDate, LocalDate.parse(startDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(ConsultationSlot::getSlotDate, LocalDate.parse(endDate));
        }
        wrapper.orderByAsc(ConsultationSlot::getSlotDate).orderByAsc(ConsultationSlot::getStartTime);

        List<ConsultationSlot> slots = slotService.list(wrapper);
        updateExpiredSlots(slots);
        return slots;
    }

    @GetMapping("/slots/list")
    public List<Map<String, Object>> getAvailableSlots(@RequestParam(required = false) Integer teacherId,
                                                        @RequestParam(required = false) String date,
                                                        @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<ConsultationSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ConsultationSlot::getStatus, "available", "full");

        if (teacherId != null) {
            wrapper.eq(ConsultationSlot::getTeacherId, teacherId);
        }
        if (date != null && !date.isEmpty()) {
            wrapper.eq(ConsultationSlot::getSlotDate, LocalDate.parse(date));
        } else {
            wrapper.ge(ConsultationSlot::getSlotDate, LocalDate.now());
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(ConsultationSlot::getTeacherName, keyword)
                    .or().like(ConsultationSlot::getRemark, keyword)
                    .or().like(ConsultationSlot::getLocation, keyword));
        }
        wrapper.orderByAsc(ConsultationSlot::getSlotDate).orderByAsc(ConsultationSlot::getStartTime);

        List<ConsultationSlot> slots = slotService.list(wrapper);
        updateExpiredSlots(slots);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ConsultationSlot slot : slots) {
            if ("expired".equals(slot.getStatus()) || "closed".equals(slot.getStatus())) {
                continue;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", slot.getId());
            map.put("teacherId", slot.getTeacherId());
            map.put("teacherName", slot.getTeacherName());
            map.put("slotDate", slot.getSlotDate());
            map.put("startTime", slot.getStartTime());
            map.put("endTime", slot.getEndTime());
            map.put("location", slot.getLocation());
            map.put("locationType", slot.getLocationType());
            map.put("capacity", slot.getCapacity());
            map.put("bookedCount", slot.getBookedCount());
            map.put("status", slot.getStatus());
            map.put("remark", slot.getRemark());
            map.put("available", slot.getCapacity() - slot.getBookedCount());
            result.add(map);
        }
        return result;
    }

    @GetMapping("/slot/detail")
    public Map<String, Object> getSlotDetail(@RequestParam Integer slotId,
                                              @RequestParam(required = false) Integer studentId) {
        ConsultationSlot slot = slotService.getById(slotId);
        if (slot == null) {
            return null;
        }
        updateExpiredSlot(slot);

        Map<String, Object> result = new HashMap<>();
        result.put("slot", slot);
        result.put("available", slot.getCapacity() - slot.getBookedCount());

        if (studentId != null) {
            LambdaQueryWrapper<ConsultationBooking> bookingWrapper = new LambdaQueryWrapper<>();
            bookingWrapper.eq(ConsultationBooking::getSlotId, slotId)
                    .eq(ConsultationBooking::getStudentId, studentId)
                    .ne(ConsultationBooking::getStatus, "cancelled");
            ConsultationBooking booking = bookingService.getOne(bookingWrapper);
            result.put("hasBooked", booking != null);
            result.put("myBooking", booking);
        }

        return result;
    }

    @PostMapping("/slot/save")
    public boolean saveSlot(@RequestBody ConsultationSlot slot) {
        if (slot.getTeacherId() != null && slot.getTeacherName() == null) {
            Teacher teacher = teacherService.getById(slot.getTeacherId());
            if (teacher != null) {
                slot.setTeacherName(teacher.getTname());
            }
        }
        if (slot.getStatus() == null) {
            slot.setStatus("available");
        }
        if (slot.getBookedCount() == null) {
            slot.setBookedCount(0);
        }
        if (slot.getVersion() == null) {
            slot.setVersion(0);
        }

        boolean result = slotService.saveOrUpdate(slot);
        if (result) {
            refreshSlotStatus(slot.getId());
        }
        return result;
    }

    @PostMapping("/slot/delete")
    public boolean deleteSlot(@RequestParam Integer slotId) {
        ConsultationSlot slot = slotService.getById(slotId);
        if (slot == null) return false;
        if (slot.getBookedCount() != null && slot.getBookedCount() > 0) {
            throw new RuntimeException("该时段已有学生预约，无法删除");
        }
        return slotService.removeById(slotId);
    }

    @PostMapping("/slot/toggle-status")
    public boolean toggleSlotStatus(@RequestParam Integer slotId, @RequestParam String status) {
        ConsultationSlot slot = slotService.getById(slotId);
        if (slot == null) return false;
        slot.setStatus(status);
        return slotService.updateById(slot);
    }

    @GetMapping("/bookings/slot")
    public List<ConsultationBooking> getSlotBookings(@RequestParam Integer slotId,
                                                      @RequestParam(required = false) String status) {
        LambdaQueryWrapper<ConsultationBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationBooking::getSlotId, slotId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ConsultationBooking::getStatus, status);
        }
        wrapper.ne(ConsultationBooking::getStatus, "cancelled");
        wrapper.orderByAsc(ConsultationBooking::getCreateTime);
        return bookingService.list(wrapper);
    }

    @GetMapping("/bookings/my")
    public List<Map<String, Object>> getMyBookings(@RequestParam Integer studentId,
                                                    @RequestParam(required = false) String status) {
        LambdaQueryWrapper<ConsultationBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationBooking::getStudentId, studentId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ConsultationBooking::getStatus, status);
        }
        wrapper.orderByDesc(ConsultationBooking::getCreateTime);

        List<ConsultationBooking> bookings = bookingService.list(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ConsultationBooking booking : bookings) {
            ConsultationSlot slot = slotService.getById(booking.getSlotId());
            Map<String, Object> map = new HashMap<>();
            map.put("booking", booking);
            map.put("slot", slot);
            map.put("canCancel", canCancelBooking(slot, booking));
            result.add(map);
        }
        return result;
    }

    @PostMapping("/book")
    @Transactional
    public Map<String, Object> book(@RequestParam Integer slotId,
                                     @RequestParam Integer studentId,
                                     @RequestParam(required = false) String question) {
        Map<String, Object> result = new HashMap<>();

        ConsultationSlot slot = slotService.getById(slotId);
        if (slot == null) {
            result.put("success", false);
            result.put("message", "时段不存在");
            return result;
        }

        updateExpiredSlot(slot);
        if ("expired".equals(slot.getStatus()) || "closed".equals(slot.getStatus())) {
            result.put("success", false);
            result.put("message", "该时段已关闭或已过期");
            return result;
        }

        if (slot.getBookedCount() >= slot.getCapacity()) {
            result.put("success", false);
            result.put("message", "该时段已满员");
            return result;
        }

        LambdaQueryWrapper<ConsultationBooking> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(ConsultationBooking::getSlotId, slotId)
                .eq(ConsultationBooking::getStudentId, studentId)
                .ne(ConsultationBooking::getStatus, "cancelled");
        ConsultationBooking existingBooking = bookingService.getOne(existingWrapper);
        if (existingBooking != null) {
            result.put("success", false);
            result.put("message", "您已预约该时段，请勿重复预约");
            result.put("bookingId", existingBooking.getId());
            return result;
        }

        int maxRetries = 3;
        boolean booked = false;
        for (int i = 0; i < maxRetries && !booked; i++) {
            slot = slotService.getById(slotId);
            if (slot.getBookedCount() >= slot.getCapacity()) {
                result.put("success", false);
                result.put("message", "该时段已满员");
                return result;
            }

            booked = slotService.incrementBookedCount(slotId, slot.getVersion());
        }

        if (!booked) {
            result.put("success", false);
            result.put("message", "预约失败，请稍后重试");
            return result;
        }

        try {
            User student = userService.getById(studentId);
            ConsultationBooking booking = new ConsultationBooking();
            booking.setSlotId(slotId);
            booking.setStudentId(studentId);
            booking.setStudentName(student != null ? student.getUsername() : "");
            booking.setStudentNo(student != null ? student.getUserno() : "");
            booking.setQuestion(question);
            booking.setStatus("booked");
            booking.setCreateTime(LocalDateTime.now());

            boolean saved = bookingService.save(booking);
            if (!saved) {
                slotService.decrementBookedCount(slotId, slot.getVersion() + 1);
                result.put("success", false);
                result.put("message", "预约失败，请稍后重试");
                return result;
            }

            refreshSlotStatus(slotId);

            result.put("success", true);
            result.put("message", "预约成功");
            result.put("bookingId", booking.getId());
            return result;
        } catch (Exception e) {
            try {
                slotService.decrementBookedCount(slotId, slot.getVersion() + 1);
            } catch (Exception ex) {
                // ignore
            }
            throw e;
        }
    }

    @PostMapping("/cancel")
    @Transactional
    public Map<String, Object> cancelBooking(@RequestParam Integer bookingId,
                                              @RequestParam(required = false) String reason) {
        Map<String, Object> result = new HashMap<>();

        ConsultationBooking booking = bookingService.getById(bookingId);
        if (booking == null) {
            result.put("success", false);
            result.put("message", "预约记录不存在");
            return result;
        }

        if (!"booked".equals(booking.getStatus())) {
            result.put("success", false);
            result.put("message", "该预约状态不允许取消");
            return result;
        }

        ConsultationSlot slot = slotService.getById(booking.getSlotId());
        if (!canCancelBooking(slot, booking)) {
            result.put("success", false);
            result.put("message", "已超过取消时间，无法取消");
            return result;
        }

        int maxRetries = 3;
        boolean refunded = false;
        for (int i = 0; i < maxRetries && !refunded; i++) {
            slot = slotService.getById(booking.getSlotId());
            refunded = slotService.decrementBookedCount(slot.getId(), slot.getVersion());
        }

        if (!refunded) {
            result.put("success", false);
            result.put("message", "取消失败，请稍后重试");
            return result;
        }

        try {
            booking.setStatus("cancelled");
            booking.setCancelTime(LocalDateTime.now());
            booking.setCancelReason(reason);
            bookingService.updateById(booking);

            refreshSlotStatus(slot.getId());

            result.put("success", true);
            result.put("message", "取消成功");
            return result;
        } catch (Exception e) {
            try {
                slotService.incrementBookedCount(slot.getId(), slot.getVersion() + 1);
            } catch (Exception ex) {
                // ignore
            }
            throw e;
        }
    }

    @PostMapping("/booking/update-status")
    public Map<String, Object> updateBookingStatus(@RequestParam Integer bookingId,
                                                    @RequestParam String status,
                                                    @RequestParam(required = false) String teacherRemark) {
        Map<String, Object> result = new HashMap<>();

        ConsultationBooking booking = bookingService.getById(bookingId);
        if (booking == null) {
            result.put("success", false);
            result.put("message", "预约记录不存在");
            return result;
        }

        if (!"booked".equals(booking.getStatus()) && !"completed".equals(booking.getStatus())
                && !"no_show".equals(booking.getStatus())) {
            result.put("success", false);
            result.put("message", "该状态不允许修改");
            return result;
        }

        booking.setStatus(status);
        if ("completed".equals(status) || "no_show".equals(status)) {
            booking.setCompleteTime(LocalDateTime.now());
        }
        if (teacherRemark != null) {
            booking.setTeacherRemark(teacherRemark);
        }

        boolean updated = bookingService.updateById(booking);
        result.put("success", updated);
        result.put("message", updated ? "状态更新成功" : "状态更新失败");
        return result;
    }

    @GetMapping("/teachers")
    public List<Teacher> getTeachersWithSlots() {
        List<ConsultationSlot> slots = slotService.list(
                new LambdaQueryWrapper<ConsultationSlot>()
                        .in(ConsultationSlot::getStatus, "available", "full")
                        .ge(ConsultationSlot::getSlotDate, LocalDate.now())
        );

        List<Integer> teacherIds = new ArrayList<>();
        for (ConsultationSlot slot : slots) {
            if (!teacherIds.contains(slot.getTeacherId())) {
                teacherIds.add(slot.getTeacherId());
            }
        }

        if (teacherIds.isEmpty()) {
            return new ArrayList<>();
        }

        return teacherService.listByIds(teacherIds);
    }

    @GetMapping("/stats/teacher")
    public Map<String, Object> getTeacherStats(@RequestParam Integer teacherId) {
        Map<String, Object> stats = new HashMap<>();

        LocalDate today = LocalDate.now();

        long totalSlots = slotService.count(new LambdaQueryWrapper<ConsultationSlot>()
                .eq(ConsultationSlot::getTeacherId, teacherId));
        stats.put("totalSlots", totalSlots);

        long availableSlots = slotService.count(new LambdaQueryWrapper<ConsultationSlot>()
                .eq(ConsultationSlot::getTeacherId, teacherId)
                .eq(ConsultationSlot::getStatus, "available"));
        stats.put("availableSlots", availableSlots);

        long todaySlots = slotService.count(new LambdaQueryWrapper<ConsultationSlot>()
                .eq(ConsultationSlot::getTeacherId, teacherId)
                .eq(ConsultationSlot::getSlotDate, today));
        stats.put("todaySlots", todaySlots);

        List<ConsultationSlot> teacherSlots = slotService.list(
                new LambdaQueryWrapper<ConsultationSlot>().eq(ConsultationSlot::getTeacherId, teacherId));

        int totalBookings = 0;
        int completedBookings = 0;
        int noShowBookings = 0;

        for (ConsultationSlot slot : teacherSlots) {
            List<ConsultationBooking> bookings = bookingService.list(
                    new LambdaQueryWrapper<ConsultationBooking>()
                            .eq(ConsultationBooking::getSlotId, slot.getId())
                            .ne(ConsultationBooking::getStatus, "cancelled"));
            totalBookings += bookings.size();

            for (ConsultationBooking b : bookings) {
                if ("completed".equals(b.getStatus())) completedBookings++;
                if ("no_show".equals(b.getStatus())) noShowBookings++;
            }
        }

        stats.put("totalBookings", totalBookings);
        stats.put("completedBookings", completedBookings);
        stats.put("noShowBookings", noShowBookings);

        return stats;
    }

    @GetMapping("/stats/student")
    public Map<String, Object> getStudentStats(@RequestParam Integer studentId) {
        Map<String, Object> stats = new HashMap<>();

        long totalBookings = bookingService.count(new LambdaQueryWrapper<ConsultationBooking>()
                .eq(ConsultationBooking::getStudentId, studentId));
        stats.put("totalBookings", totalBookings);

        long bookedCount = bookingService.count(new LambdaQueryWrapper<ConsultationBooking>()
                .eq(ConsultationBooking::getStudentId, studentId)
                .eq(ConsultationBooking::getStatus, "booked"));
        stats.put("bookedCount", bookedCount);

        long completedCount = bookingService.count(new LambdaQueryWrapper<ConsultationBooking>()
                .eq(ConsultationBooking::getStudentId, studentId)
                .eq(ConsultationBooking::getStatus, "completed"));
        stats.put("completedCount", completedCount);

        long cancelledCount = bookingService.count(new LambdaQueryWrapper<ConsultationBooking>()
                .eq(ConsultationBooking::getStudentId, studentId)
                .eq(ConsultationBooking::getStatus, "cancelled"));
        stats.put("cancelledCount", cancelledCount);

        long noShowCount = bookingService.count(new LambdaQueryWrapper<ConsultationBooking>()
                .eq(ConsultationBooking::getStudentId, studentId)
                .eq(ConsultationBooking::getStatus, "no_show"));
        stats.put("noShowCount", noShowCount);

        return stats;
    }

    private boolean canCancelBooking(ConsultationSlot slot, ConsultationBooking booking) {
        if (slot == null || booking == null) return false;
        if (!"booked".equals(booking.getStatus())) return false;

        LocalDateTime slotStartDateTime = LocalDateTime.of(slot.getSlotDate(), slot.getStartTime());
        return LocalDateTime.now().isBefore(slotStartDateTime);
    }

    private void updateExpiredSlots(List<ConsultationSlot> slots) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        boolean needUpdate = false;

        for (ConsultationSlot slot : slots) {
            if ("expired".equals(slot.getStatus()) || "closed".equals(slot.getStatus())) {
                continue;
            }

            LocalDate slotDate = slot.getSlotDate();
            LocalTime endTime = slot.getEndTime();

            if (slotDate.isBefore(today) || (slotDate.isEqual(today) && endTime.isBefore(now))) {
                slot.setStatus("expired");
                needUpdate = true;
            }
        }

        if (needUpdate) {
            for (ConsultationSlot slot : slots) {
                if ("expired".equals(slot.getStatus())) {
                    slotService.updateById(slot);
                }
            }
        }
    }

    private void updateExpiredSlot(ConsultationSlot slot) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if ("expired".equals(slot.getStatus()) || "closed".equals(slot.getStatus())) {
            return;
        }

        LocalDate slotDate = slot.getSlotDate();
        LocalTime endTime = slot.getEndTime();

        if (slotDate.isBefore(today) || (slotDate.isEqual(today) && endTime.isBefore(now))) {
            slot.setStatus("expired");
            slotService.updateById(slot);
        }
    }

    private void refreshSlotStatus(Integer slotId) {
        ConsultationSlot slot = slotService.getById(slotId);
        if (slot == null) return;

        updateExpiredSlot(slot);

        if ("expired".equals(slot.getStatus()) || "closed".equals(slot.getStatus())) {
            return;
        }

        String newStatus;
        if (slot.getBookedCount() >= slot.getCapacity()) {
            newStatus = "full";
        } else {
            newStatus = "available";
        }

        if (!newStatus.equals(slot.getStatus())) {
            slot.setStatus(newStatus);
            slotService.updateById(slot);
        }
    }
}
