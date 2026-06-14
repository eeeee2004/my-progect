package com.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hotel.common.annotation.OperationLog;
import com.hotel.common.exception.NotFoundException;
import com.hotel.dto.OccupiedRoomDTO;
import com.hotel.dto.RoomQueryDTO;
import com.hotel.entity.BookingOrder;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.mapper.BookingOrderMapper;
import com.hotel.mapper.RoomMapper;
import com.hotel.mapper.RoomTypeMapper;
import com.hotel.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    private final RoomTypeMapper roomTypeMapper;
    private final BookingOrderMapper bookingOrderMapper;

    @Override
    public IPage<Map<String, Object>> queryAvailableRooms(RoomQueryDTO queryDTO, int page, int pageSize) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getRoomTypeId() != null) {
            wrapper.eq(Room::getRoomTypeId, queryDTO.getRoomTypeId());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Room::getStatus, queryDTO.getStatus());
        }
        IPage<Room> roomPage = this.page(new Page<>(page, pageSize), wrapper);
        List<Map<String, Object>> records = new ArrayList<>();
        for (Room room : roomPage.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", room.getId());
            map.put("roomNumber", room.getRoomNumber());
            map.put("floor", room.getFloor());
            map.put("roomTypeId", room.getRoomTypeId());
            map.put("status", room.getStatus());
            map.put("remark", room.getRemark());
            map.put("createTime", room.getCreateTime());
            RoomType roomType = roomTypeMapper.selectById(room.getRoomTypeId());
            if (roomType != null) {
                map.put("roomTypeName", roomType.getName());
                map.put("bedType", roomType.getBedType());
                map.put("area", roomType.getArea());
                map.put("basePrice", roomType.getBasePrice());
                map.put("facilities", roomType.getFacilities());
                map.put("description", roomType.getDescription());
                map.put("imageUrl", roomType.getImageUrl());
            }
            records.add(map);
        }
        IPage<Map<String, Object>> resultPage = new Page<>(page, pageSize, roomPage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public IPage<Room> getAllRooms(int page, int pageSize) {
        return this.page(new Page<>(page, pageSize));
    }

    @Override
    public Room getRoomDetail(Long id) {
        return this.getById(id);
    }

    @Override
    @OperationLog(module = "房间管理", action = "更新房间状态", description = "更新房间的占用状态")
    public Room updateRoomStatus(Long id, String status) {
        Room room = this.getById(id);
        if (room == null) {
            throw new NotFoundException("房间", id);
        }
        room.setStatus(status);
        this.updateById(room);
        return room;
    }

    @Override
    public List<OccupiedRoomDTO> getOccupiedRooms() {
        List<Room> allRooms = this.list();
        List<BookingOrder> activeOrders = bookingOrderMapper.selectList(
            new LambdaQueryWrapper<BookingOrder>()
                .in(BookingOrder::getStatus, "CHECKED_IN", "PENDING")
                .orderByDesc(BookingOrder::getCreateTime)
        );

        Map<Long, BookingOrder> roomOrderMap = activeOrders.stream()
            .filter(o -> o.getRoomId() != null)
            .collect(Collectors.toMap(BookingOrder::getRoomId, o -> o, (a, b) -> a));

        List<OccupiedRoomDTO> result = new ArrayList<>();
        for (Room room : allRooms) {
            if ("OCCUPIED".equals(room.getStatus()) || "BOOKED".equals(room.getStatus())) {
                BookingOrder order = roomOrderMap.get(room.getId());
                RoomType rt = roomTypeMapper.selectById(room.getRoomTypeId());
                result.add(new OccupiedRoomDTO(
                    room.getId(), room.getRoomNumber(), room.getFloor(),
                    room.getRoomTypeId(), rt != null ? rt.getName() : "",
                    room.getStatus(),
                    order != null ? order.getGuestName() : "",
                    order != null ? order.getGuestPhone() : "",
                    order != null ? order.getId() : null,
                    order != null ? order.getOrderNo() : "",
                    order != null ? order.getCheckInDate() : null,
                    order != null ? order.getCheckOutDate() : null,
                    rt != null ? rt.getBasePrice() : null
                ));
            }
        }
        return result;
    }
}
