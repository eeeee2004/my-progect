package com.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("room_type")
public class RoomType {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String bedType;
    private BigDecimal area;
    private BigDecimal basePrice;
    private Integer capacity;
    private String checkInTime;
    private String checkOutTime;
    private String breakfast;
    private String windowType;
    private String floorInfo;
    private String cancelPolicy;
    private String extraInfo;
    private String description;
    private String facilities;
    private String imageUrl;
    private Integer sortOrder;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
