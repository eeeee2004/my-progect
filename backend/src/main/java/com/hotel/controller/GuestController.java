package com.hotel.controller;

import com.hotel.common.PageResult;
import com.hotel.common.Result;
import com.hotel.dto.BookingDTO;
import com.hotel.entity.BookingOrder;
import com.hotel.entity.SysUser;
import com.hotel.service.BookingOrderService;
import com.hotel.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guest")
@RequiredArgsConstructor
public class GuestController {

    private final BookingOrderService bookingOrderService;
    private final SysUserService sysUserService;

    @GetMapping("/profile")
    public Result<SysUser> profile() {
        Long userId = getCurrentUserId();
        return Result.success(sysUserService.getCurrentUser(userId));
    }

    @PutMapping("/profile")
    public Result<SysUser> updateProfile(@RequestBody SysUser user) {
        Long userId = getCurrentUserId();
        return Result.success(sysUserService.updateProfile(userId, user));
    }

    @PostMapping("/bookings")
    public Result<BookingOrder> createBooking(@Valid @RequestBody BookingDTO bookingDTO) {
        Long userId = getCurrentUserId();
        return Result.success("预订成功", bookingOrderService.createBooking(userId, bookingDTO));
    }

    @PutMapping("/bookings/{id}/cancel")
    public Result<BookingOrder> cancelBooking(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return Result.success("订单已取消", bookingOrderService.cancelBooking(userId, id));
    }

    @GetMapping("/bookings")
    public Result<PageResult<BookingOrder>> getUserOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = getCurrentUserId();
        List<BookingOrder> records = bookingOrderService.getUserOrders(userId, status, page, pageSize);
        return Result.success(PageResult.of(records, 0, page, pageSize));
    }

    @GetMapping("/bookings/{id}")
    public Result<BookingOrder> getOrderDetail(@PathVariable Long id) {
        return Result.success(bookingOrderService.getById(id));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
