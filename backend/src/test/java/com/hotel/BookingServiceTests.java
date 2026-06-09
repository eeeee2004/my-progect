package com.hotel;

import com.hotel.common.exception.BusinessException;
import com.hotel.dto.BookingDTO;
import com.hotel.entity.BookingOrder;
import com.hotel.entity.Room;
import com.hotel.service.BookingOrderService;
import com.hotel.service.RoomService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookingServiceTests {

    @Autowired
    private BookingOrderService bookingOrderService;

    @Autowired
    private RoomService roomService;

    private static Long testOrderId;

    @Test
    @Order(1)
    void createBookingSuccess() {
        List<Room> rooms = roomService.getAllRooms(1, 100).getRecords();
        Room availableRoom = rooms.stream()
                .filter(r -> "AVAILABLE".equals(r.getStatus()))
                .findFirst().orElse(null);
        assertNotNull(availableRoom, "至少需要有一间空闲房间");

        BookingDTO dto = new BookingDTO();
        dto.setRoomId(availableRoom.getId());
        dto.setCheckInDate(LocalDate.now().plusDays(1));
        dto.setCheckOutDate(LocalDate.now().plusDays(3));
        dto.setGuestName("预订测试");
        dto.setGuestPhone("13800009999");

        BookingOrder order = bookingOrderService.createBooking(1L, dto);

        assertNotNull(order);
        assertNotNull(order.getOrderNo());
        assertEquals("PENDING", order.getStatus());
        assertTrue(order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(2, order.getNights());

        testOrderId = order.getId();

        Room room = roomService.getRoomDetail(availableRoom.getId());
        assertEquals("BOOKED", room.getStatus());
    }

    @Test
    @Order(2)
    void cancelBooking() {
        assertNotNull(testOrderId);

        BookingOrder order = bookingOrderService.cancelBooking(1L, testOrderId);
        assertEquals("CANCELLED", order.getStatus());

        Room room = roomService.getRoomDetail(order.getRoomId());
        assertEquals("AVAILABLE", room.getStatus());
    }

    @Test
    @Order(3)
    void createBookingWithInvalidDate() {
        List<Room> rooms = roomService.getAllRooms(1, 100).getRecords();
        Room availableRoom = rooms.stream()
                .filter(r -> "AVAILABLE".equals(r.getStatus()))
                .findFirst().orElse(null);
        assertNotNull(availableRoom);

        BookingDTO dto = new BookingDTO();
        dto.setRoomId(availableRoom.getId());
        dto.setCheckInDate(LocalDate.now().minusDays(1));
        dto.setCheckOutDate(LocalDate.now().plusDays(1));
        dto.setGuestName("测试");
        dto.setGuestPhone("13800000000");

        assertThrows(BusinessException.class, () -> bookingOrderService.createBooking(1L, dto));
    }

    @Test
    @Order(4)
    void getUserOrders() {
        List<BookingOrder> orders = bookingOrderService.getUserOrders(1L, null, 1, 10);
        assertNotNull(orders);
    }
}
