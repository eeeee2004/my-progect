package com.hotel.controller;

import com.hotel.common.PageResult;
import com.hotel.common.Result;
import com.hotel.dto.CheckInDTO;
import com.hotel.dto.CheckOutDTO;
import com.hotel.entity.BookingOrder;
import com.hotel.entity.Room;
import com.hotel.service.BookingOrderService;
import com.hotel.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/front-desk")
@RequiredArgsConstructor
public class FrontDeskController {

    private final RoomService roomService;
    private final BookingOrderService bookingOrderService;

    @GetMapping("/rooms")
    public Result<PageResult<Room>> getAllRooms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        var pageResult = roomService.getAllRooms(page, pageSize);
        return Result.success(PageResult.of(pageResult.getRecords(), pageResult.getTotal(), page, pageSize));
    }

    @PutMapping("/rooms/{id}/status")
    public Result<Room> updateRoomStatus(@PathVariable Long id, @RequestParam String status) {
        return Result.success(roomService.updateRoomStatus(id, status));
    }

    @GetMapping("/rooms/{id}")
    public Result<Room> getRoomDetail(@PathVariable Long id) {
        return Result.success(roomService.getRoomDetail(id));
    }

    @GetMapping("/orders")
    public Result<PageResult<BookingOrder>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<BookingOrder> records = bookingOrderService.getFrontDeskOrders(status, page, pageSize);
        return Result.success(PageResult.of(records, 0, page, pageSize));
    }

    @PostMapping("/check-in")
    public Result<BookingOrder> checkIn(@RequestBody CheckInDTO checkInDTO) {
        return Result.success("入住办理成功", bookingOrderService.checkIn(checkInDTO));
    }

    @PostMapping("/check-out/{orderId}")
    public Result<Map<String, Object>> checkOut(@PathVariable Long orderId, @RequestBody CheckOutDTO checkOutDTO) {
        return Result.success("退房结算完成", bookingOrderService.checkOut(orderId, checkOutDTO));
    }

    @GetMapping("/today-arrivals")
    public Result<List<BookingOrder>> getTodayArrivals() {
        return Result.success(bookingOrderService.getTodayArrivals());
    }

    @GetMapping("/today-departures")
    public Result<List<BookingOrder>> getTodayDepartures() {
        return Result.success(bookingOrderService.getTodayDepartures());
    }

    @GetMapping("/orders/{id}")
    public Result<BookingOrder> getOrderDetail(@PathVariable Long id) {
        return Result.success(bookingOrderService.getById(id));
    }
}
