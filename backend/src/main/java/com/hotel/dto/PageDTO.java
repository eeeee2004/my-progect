package com.hotel.dto;

import lombok.Data;

@Data
public class PageDTO {
    private int page = 1;
    private int pageSize = 10;
}
