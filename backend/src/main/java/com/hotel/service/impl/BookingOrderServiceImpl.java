package com.hotel.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hotel.common.annotation.OperationLog;
import com.hotel.common.exception.BusinessException;
import com.hotel.common.exception.ForbiddenException;
import com.hotel.common.exception.NotFoundException;
import com.hotel.dto.BookingDTO;
import com.hotel.dto.CheckInDTO;
import com.hotel.dto.CheckOutDTO;
import com.hotel.entity.BookingOrder;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.mapper.BookingOrderMapper;
import com.hotel.mapper.RoomMapper;
import com.hotel.mapper.RoomTypeMapper;
import com.hotel.service.BookingOrderService;
import com.hotel.service.PriceCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookingOrderServiceImpl extends ServiceImpl<BookingOrderMapper, BookingOrder> implements BookingOrderService {

    private final RoomMapper roomMapper;
    private final RoomTypeMapper roomTypeMapper;
    private final PriceCalendarService priceCalendarService;

    @Override
    @Transactional
    @OperationLog(module = "订单管理", action = "创建预订", description = "住客在线预订房间")
    public BookingOrder createBooking(Long userId, BookingDTO bookingDTO) {
        Room room = roomMapper.selectById(bookingDTO.getRoomId());
        if (room == null) {
            throw new NotFoundException("房间", bookingDTO.getRoomId());
        }
        if (!"AVAILABLE".equals(room.getStatus())) {
            throw new BusinessException("该房间当前不可预订");
        }
        if (bookingDTO.getCheckInDate().isBefore(LocalDate.now())) {
            throw new BusinessException("入住日期不能早于今天");
        }
        if (bookingDTO.getCheckOutDate().isBefore(bookingDTO.getCheckInDate().plusDays(1))) {
            throw new BusinessException("离店日期必须晚于入住日期至少一天");
        }

        RoomType roomType = roomTypeMapper.selectById(room.getRoomTypeId());
        long nights = ChronoUnit.DAYS.between(bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (int i = 0; i < nights; i++) {
            LocalDate date = bookingDTO.getCheckInDate().plusDays(i);
            totalAmount = totalAmount.add(priceCalendarService.calculatePrice(room.getRoomTypeId(), roomType.getBasePrice(), date));
        }

        BookingOrder order = new BookingOrder();
        order.setOrderNo(IdUtil.fastSimpleUUID().substring(0, 16).toUpperCase());
        order.setUserId(userId);
        order.setRoomId(room.getId());
        order.setCheckInDate(bookingDTO.getCheckInDate());
        order.setCheckOutDate(bookingDTO.getCheckOutDate());
        order.setNights((int) nights);
        order.setTotalAmount(totalAmount);
        order.setGuestName(bookingDTO.getGuestName());
        order.setGuestPhone(bookingDTO.getGuestPhone());
        order.setGuestIdCard(bookingDTO.getGuestIdCard());
        order.setStatus("PENDING");

        this.save(order);

        room.setStatus("BOOKED");
        roomMapper.updateById(room);

        return order;
    }

    @Override
    @Transactional
    @OperationLog(module = "订单管理", action = "取消订单", description = "住客取消预订订单")
    public BookingOrder cancelBooking(Long userId, Long orderId) {
        BookingOrder order = this.getById(orderId);
        if (order == null) {
            throw new NotFoundException("订单", orderId);
        }
        if (!order.getUserId().equals(userId)) {
            throw new ForbiddenException("无权操作此订单");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("仅可取消待入住的订单");
        }
        order.setStatus("CANCELLED");
        this.updateById(order);

        Room room = roomMapper.selectById(order.getRoomId());
        if (room != null) {
            room.setStatus("AVAILABLE");
            roomMapper.updateById(room);
        }
        return order;
    }

    @Override
    public List<BookingOrder> getUserOrders(Long userId, String status, int page, int pageSize) {
        LambdaQueryWrapper<BookingOrder> wrapper = new LambdaQueryWrapper<BookingOrder>()
                .eq(BookingOrder::getUserId, userId)
                .orderByDesc(BookingOrder::getCreateTime);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(BookingOrder::getStatus, status);
        }
        return this.page(new Page<>(page, pageSize), wrapper).getRecords();
    }

    @Override
    @Transactional
    @OperationLog(module = "前台管理", action = "办理入住", description = "前台办理客人入住手续")
    public BookingOrder checkIn(CheckInDTO checkInDTO) {
        BookingOrder order;
        if (checkInDTO.getOrderId() != null) {
            order = this.getById(checkInDTO.getOrderId());
            if (order == null) {
                throw new NotFoundException("订单", checkInDTO.getOrderId());
            }
            if (!"PENDING".equals(order.getStatus())) {
                throw new BusinessException("订单状态不允许办理入住");
            }
        } else if (checkInDTO.getRoomId() != null) {
            Room room = roomMapper.selectById(checkInDTO.getRoomId());
            if (room == null) {
                throw new NotFoundException("房间", checkInDTO.getRoomId());
            }
            if (!"AVAILABLE".equals(room.getStatus())) {
                throw new BusinessException("该房间当前不可入住");
            }
            RoomType roomType = roomTypeMapper.selectById(room.getRoomTypeId());
            order = new BookingOrder();
            order.setOrderNo(IdUtil.fastSimpleUUID().substring(0, 16).toUpperCase());
            order.setRoomId(room.getId());
            order.setCheckInDate(LocalDate.now());
            order.setCheckOutDate(LocalDate.now().plusDays(1));
            order.setNights(1);
            order.setTotalAmount(roomType.getBasePrice());
            order.setGuestName(checkInDTO.getGuestName());
            order.setGuestPhone(checkInDTO.getGuestPhone());
            order.setGuestIdCard(checkInDTO.getGuestIdCard());
            order.setStatus("PENDING");
            this.save(order);
        } else {
            throw new BusinessException("请提供订单ID或房间ID");
        }

        order.setStatus("CHECKED_IN");
        this.updateById(order);

        Room room = roomMapper.selectById(order.getRoomId());
        if (room != null) {
            room.setStatus("OCCUPIED");
            roomMapper.updateById(room);
        }
        return order;
    }

    @Override
    @Transactional
    @OperationLog(module = "前台管理", action = "退房结算", description = "前台办理退房并生成账单")
    public Map<String, Object> checkOut(Long orderId, CheckOutDTO checkOutDTO) {
        BookingOrder order = this.getById(orderId);
        if (order == null) {
            throw new NotFoundException("订单", orderId);
        }
        if (!"CHECKED_IN".equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许退房");
        }

        long actualNights = Math.max(1, ChronoUnit.DAYS.between(order.getCheckInDate(), LocalDate.now()));
        Room room = roomMapper.selectById(order.getRoomId());
        RoomType roomType = roomTypeMapper.selectById(room.getRoomTypeId());
        BigDecimal roomCharge = BigDecimal.ZERO;
        for (int i = 0; i < actualNights; i++) {
            LocalDate date = order.getCheckInDate().plusDays(i);
            roomCharge = roomCharge.add(priceCalendarService.calculatePrice(room.getRoomTypeId(), roomType.getBasePrice(), date));
        }

        BigDecimal extraCharge = checkOutDTO.getExtraCharge() != null ? checkOutDTO.getExtraCharge() : BigDecimal.ZERO;
        BigDecimal totalAmount = roomCharge.add(extraCharge);

        order.setCheckOutDate(LocalDate.now());
        order.setNights((int) actualNights);
        order.setTotalAmount(totalAmount);
        order.setStatus("COMPLETED");
        this.updateById(order);

        room.setStatus("CLEANING");
        roomMapper.updateById(room);

        Map<String, Object> bill = new HashMap<>();
        bill.put("orderNo", order.getOrderNo());
        bill.put("guestName", order.getGuestName());
        bill.put("roomNumber", room.getRoomNumber());
        bill.put("checkInDate", order.getCheckInDate());
        bill.put("checkOutDate", LocalDate.now());
        bill.put("nights", actualNights);
        bill.put("roomCharge", roomCharge);
        bill.put("extraCharge", extraCharge);
        bill.put("totalAmount", totalAmount);
        bill.put("status", "COMPLETED");
        return bill;
    }

    @Override
    public List<BookingOrder> getFrontDeskOrders(String status, int page, int pageSize) {
        LambdaQueryWrapper<BookingOrder> wrapper = new LambdaQueryWrapper<BookingOrder>()
                .orderByDesc(BookingOrder::getCreateTime);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(BookingOrder::getStatus, status);
        }
        Page<BookingOrder> result = this.page(new Page<>(page, pageSize), wrapper);
        return result.getRecords();
    }

    @Override
    public long countFrontDeskOrders(String status) {
        LambdaQueryWrapper<BookingOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(BookingOrder::getStatus, status);
        }
        return this.count(wrapper);
    }

    @Override
    public List<BookingOrder> getTodayArrivals() {
        return this.list(new LambdaQueryWrapper<BookingOrder>()
                .eq(BookingOrder::getCheckInDate, LocalDate.now())
                .eq(BookingOrder::getStatus, "PENDING")
                .orderByDesc(BookingOrder::getCreateTime));
    }

    @Override
    public List<BookingOrder> getTodayDepartures() {
        return this.list(new LambdaQueryWrapper<BookingOrder>()
                .le(BookingOrder::getCheckOutDate, LocalDate.now())
                .eq(BookingOrder::getStatus, "CHECKED_IN")
                .orderByDesc(BookingOrder::getCreateTime));
    }
}
