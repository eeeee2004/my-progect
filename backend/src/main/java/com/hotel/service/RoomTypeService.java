package com.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hotel.entity.RoomType;

import java.util.List;

public interface RoomTypeService extends IService<RoomType> {
    List<RoomType> getEnabledRoomTypes();
}
