package com.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hotel.dto.BookingDTO;
import com.hotel.dto.CheckInDTO;
import com.hotel.dto.CheckOutDTO;
import com.hotel.entity.BookingOrder;

import java.util.List;
import java.util.Map;

public interface BookingOrderService extends IService<BookingOrder> {
    BookingOrder createBooking(Long userId, BookingDTO bookingDTO);
    BookingOrder cancelBooking(Long userId, Long orderId);
    List<BookingOrder> getUserOrders(Long userId, String status, int page, int pageSize);
    BookingOrder checkIn(CheckInDTO checkInDTO);
    Map<String, Object> checkOut(Long orderId, CheckOutDTO checkOutDTO);
    List<BookingOrder> getFrontDeskOrders(String status, int page, int pageSize);
    long countFrontDeskOrders(String status);
    List<BookingOrder> getTodayArrivals();
    List<BookingOrder> getTodayDepartures();
}
