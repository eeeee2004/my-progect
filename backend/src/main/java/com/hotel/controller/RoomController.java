package com.hotel.controller;

import com.hotel.common.PageResult;
import com.hotel.common.Result;
import com.hotel.dto.RoomQueryDTO;
import com.hotel.entity.RoomType;
import com.hotel.service.PriceCalendarService;
import com.hotel.service.RoomService;
import com.hotel.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final RoomTypeService roomTypeService;
    private final PriceCalendarService priceCalendarService;

    @GetMapping("/rooms")
    public Result<PageResult<Map<String, Object>>> queryRooms(
            RoomQueryDTO queryDTO,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        var pageResult = roomService.queryAvailableRooms(queryDTO, page, pageSize);
        return Result.success(PageResult.of(pageResult.getRecords(), pageResult.getTotal(), page, pageSize));
    }

    @GetMapping("/rooms/{id}")
    public Result<?> getRoomDetail(@PathVariable Long id) {
        return Result.success(roomService.getRoomDetail(id));
    }

    @GetMapping("/room-types")
    public Result<List<RoomType>> getAllRoomTypes() {
        return Result.success(roomTypeService.list());
    }

    @GetMapping("/room-types/{id}")
    public Result<RoomType> getRoomTypeDetail(@PathVariable Long id) {
        return Result.success(roomTypeService.getById(id));
    }

    @GetMapping("/room-types/{id}/price")
    public Result<Map<String, Object>> getRoomTypePrice(
            @PathVariable Long id,
            @RequestParam LocalDate checkInDate,
            @RequestParam LocalDate checkOutDate) {
        RoomType roomType = roomTypeService.getById(id);
        if (roomType == null) {
            return Result.error("房型不存在");
        }
        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        BigDecimal totalPrice = BigDecimal.ZERO;
        java.util.List<Map<String, Object>> dailyPrices = new java.util.ArrayList<>();
        for (int i = 0; i < nights; i++) {
            LocalDate date = checkInDate.plusDays(i);
            BigDecimal factor = priceCalendarService.getFactor(id, date);
            BigDecimal dayPrice = roomType.getBasePrice().multiply(factor).setScale(2, java.math.RoundingMode.HALF_UP);
            totalPrice = totalPrice.add(dayPrice);
            Map<String, Object> dp = new HashMap<>();
            dp.put("date", date.toString());
            dp.put("basePrice", roomType.getBasePrice());
            dp.put("factor", factor);
            dp.put("price", dayPrice);
            dailyPrices.add(dp);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("roomTypeId", id);
        result.put("basePrice", roomType.getBasePrice());
        result.put("nights", nights);
        result.put("totalPrice", totalPrice);
        result.put("dailyPrices", dailyPrices);
        return Result.success(result);
    }
}
