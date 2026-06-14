package com.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class OccupiedRoomDTO {
    private Long roomId;
    private String roomNumber;
    private Integer floor;
    private Long roomTypeId;
    private String roomTypeName;
    private String status;
    private String guestName;
    private String guestPhone;
    private Long orderId;
    private String orderNo;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal basePrice;
}
