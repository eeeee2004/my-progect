package com.hotel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.entity.BookingOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookingOrderMapper extends BaseMapper<BookingOrder> {
}
