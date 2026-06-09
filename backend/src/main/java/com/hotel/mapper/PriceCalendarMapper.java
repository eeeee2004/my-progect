package com.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.entity.PriceCalendar;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface PriceCalendarMapper extends BaseMapper<PriceCalendar> {

    @Select("SELECT COALESCE(MAX(price_factor), 1.00) FROM price_calendar " +
            "WHERE room_type_id = #{roomTypeId} AND status = 1 " +
            "AND start_date <= #{date} AND end_date >= #{date} " +
            "ORDER BY priority DESC LIMIT 1")
    java.math.BigDecimal findFactorByDate(Long roomTypeId, LocalDate date);
}
