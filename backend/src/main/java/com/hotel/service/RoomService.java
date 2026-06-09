package com.hotel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hotel.dto.RoomQueryDTO;
import com.hotel.entity.Room;

import java.util.List;
import java.util.Map;

public interface RoomService extends IService<Room> {
    IPage<Map<String, Object>> queryAvailableRooms(RoomQueryDTO queryDTO, int page, int pageSize);
    IPage<Room> getAllRooms(int page, int pageSize);
    Room getRoomDetail(Long id);
    Room updateRoomStatus(Long id, String status);
}
