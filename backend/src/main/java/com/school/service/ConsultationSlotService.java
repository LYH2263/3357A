package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.ConsultationSlot;
import com.school.mapper.ConsultationSlotMapper;
import org.springframework.stereotype.Service;

@Service
public class ConsultationSlotService extends ServiceImpl<ConsultationSlotMapper, ConsultationSlot> {

    public boolean incrementBookedCount(Integer slotId, Integer version) {
        return baseMapper.incrementBookedCount(slotId, version) > 0;
    }

    public boolean decrementBookedCount(Integer slotId, Integer version) {
        return baseMapper.decrementBookedCount(slotId, version) > 0;
    }
}
