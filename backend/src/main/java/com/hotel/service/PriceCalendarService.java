package com.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hotel.entity.PriceCalendar;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PriceCalendarService extends IService<PriceCalendar> {
    BigDecimal getFactor(Long roomTypeId, LocalDate date);
    BigDecimal calculatePrice(Long roomTypeId, BigDecimal basePrice, LocalDate date);
}
