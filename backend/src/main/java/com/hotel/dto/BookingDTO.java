package com.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookingDTO {
    @NotNull(message = "房间ID不能为空")
    private Long roomId;
    @NotNull(message = "入住日期不能为空")
    private LocalDate checkInDate;
    @NotNull(message = "离店日期不能为空")
    private LocalDate checkOutDate;
    @NotBlank(message = "入住人姓名不能为空")
    private String guestName;
    @NotBlank(message = "入住人手机号不能为空")
    private String guestPhone;
    private String guestIdCard;
}
