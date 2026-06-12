package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.entity.ConsultationSlot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ConsultationSlotMapper extends BaseMapper<ConsultationSlot> {

    @Update("UPDATE consultation_slot SET booked_count = booked_count + 1, version = version + 1 " +
            "WHERE id = #{slotId} AND version = #{version} AND booked_count < capacity AND status = 'available'")
    int incrementBookedCount(@Param("slotId") Integer slotId, @Param("version") Integer version);

    @Update("UPDATE consultation_slot SET booked_count = booked_count - 1, version = version + 1 " +
            "WHERE id = #{slotId} AND version = #{version} AND booked_count > 0")
    int decrementBookedCount(@Param("slotId") Integer slotId, @Param("version") Integer version);
}
