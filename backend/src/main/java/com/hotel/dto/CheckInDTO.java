package com.hotel.dto;

import lombok.Data;

@Data
public class CheckInDTO {
    private Long orderId;
    private Long roomId;
    private String guestName;
    private String guestPhone;
    private String guestIdCard;
    private String idCard;
    private String remark;
}
