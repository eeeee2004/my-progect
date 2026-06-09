package com.hotel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hotel.entity.PriceCalendar;
import com.hotel.mapper.PriceCalendarMapper;
import com.hotel.service.PriceCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PriceCalendarServiceImpl extends ServiceImpl<PriceCalendarMapper, PriceCalendar> implements PriceCalendarService {

    private final PriceCalendarMapper priceCalendarMapper;

    @Override
    public BigDecimal getFactor(Long roomTypeId, LocalDate date) {
        BigDecimal factor = priceCalendarMapper.findFactorByDate(roomTypeId, date);
        if (factor == null) {
            factor = isWeekend(date) ? new BigDecimal("1.15") : BigDecimal.ONE;
        }
        return factor;
    }

    @Override
    public BigDecimal calculatePrice(Long roomTypeId, BigDecimal basePrice, LocalDate date) {
        BigDecimal factor = getFactor(roomTypeId, date);
        return basePrice.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY;
    }
}
