package com.hotel;

import com.hotel.dto.CheckInDTO;
import com.hotel.dto.CheckOutDTO;
import com.hotel.entity.BookingOrder;
import com.hotel.entity.Room;
import com.hotel.service.BookingOrderService;
import com.hotel.service.RoomService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FrontDeskServiceTests {

    @Autowired
    private BookingOrderService bookingOrderService;

    @Autowired
    private RoomService roomService;

    private static Long walkinOrderId;

    @Test
    @Order(1)
    void walkinCheckIn() {
        List<Room> rooms = roomService.getAllRooms(1, 100).getRecords();
        Room availableRoom = rooms.stream()
                .filter(r -> "AVAILABLE".equals(r.getStatus()))
                .findFirst().orElse(null);
        assertNotNull(availableRoom);

        CheckInDTO dto = new CheckInDTO();
        dto.setRoomId(availableRoom.getId());
        dto.setGuestName("散客");
        dto.setGuestPhone("13900001000");
        dto.setGuestIdCard("320102199001011234");

        BookingOrder order = bookingOrderService.checkIn(dto);

        assertNotNull(order);
        assertEquals("CHECKED_IN", order.getStatus());
        walkinOrderId = order.getId();

        Room room = roomService.getRoomDetail(availableRoom.getId());
        assertEquals("OCCUPIED", room.getStatus());
    }

    @Test
    @Order(2)
    void checkOut() {
        assertNotNull(walkinOrderId);

        CheckOutDTO dto = new CheckOutDTO();
        dto.setExtraCharge(new BigDecimal("50.00"));

        Map<String, Object> bill = bookingOrderService.checkOut(walkinOrderId, dto);

        assertNotNull(bill);
        assertEquals("COMPLETED", bill.get("status"));
        assertTrue(((BigDecimal) bill.get("totalAmount")).compareTo(BigDecimal.ZERO) > 0);

        BookingOrder order = bookingOrderService.getById(walkinOrderId);
        Room room = roomService.getRoomDetail(order.getRoomId());
        assertEquals("CLEANING", room.getStatus());
    }

    @Test
    @Order(3)
    void getFrontDeskOrders() {
        List<BookingOrder> orders = bookingOrderService.getFrontDeskOrders(null, 1, 10);
        assertNotNull(orders);
    }
}
