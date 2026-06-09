package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("price_calendar")
public class PriceCalendar {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roomTypeId;
    private String ruleType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal priceFactor;
    private Integer priority;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
