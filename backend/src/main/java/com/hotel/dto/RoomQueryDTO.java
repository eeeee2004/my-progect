package com.hotel.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RoomQueryDTO {
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Long roomTypeId;
    private String status;
}
