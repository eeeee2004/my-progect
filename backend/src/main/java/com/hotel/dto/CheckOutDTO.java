package com.hotel.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CheckOutDTO {
    private BigDecimal extraCharge;
    private String remark;
}
