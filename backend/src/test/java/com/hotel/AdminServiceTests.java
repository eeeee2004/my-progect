package com.hotel;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hotel.entity.Employee;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.service.EmployeeService;
import com.hotel.service.RoomService;
import com.hotel.service.RoomTypeService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminServiceTests {

    @Autowired
    private RoomTypeService roomTypeService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private EmployeeService employeeService;

    @Test
    void getAllRoomTypes() {
        var list = roomTypeService.list();
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test
    void crudRoomType() {
        RoomType rt = new RoomType();
        rt.setName("测试房型");
        rt.setBedType("单人床");
        rt.setArea(new BigDecimal("20.00"));
        rt.setBasePrice(new BigDecimal("188.00"));
        rt.setDescription("测试用房型");
        rt.setFacilities("WiFi,空调");
        rt.setSortOrder(0);
        rt.setStatus(1);

        roomTypeService.save(rt);
        assertNotNull(rt.getId());

        rt.setName("更新后房型");
        roomTypeService.updateById(rt);
        RoomType updated = roomTypeService.getById(rt.getId());
        assertEquals("更新后房型", updated.getName());

        roomTypeService.removeById(rt.getId());
        assertNull(roomTypeService.getById(rt.getId()));
    }

    @Test
    void getAllRooms() {
        var page = roomService.getAllRooms(1, 10);
        assertNotNull(page);
        assertFalse(page.getRecords().isEmpty());
    }

    @Test
    void crudRoom() {
        RoomType rt = roomTypeService.list().get(0);

        Room room = new Room();
        room.setRoomNumber("999");
        room.setFloor(9);
        room.setRoomTypeId(rt.getId());
        room.setStatus("AVAILABLE");

        roomService.save(room);
        assertNotNull(room.getId());

        room.setRemark("测试备注");
        roomService.updateById(room);
        Room updated = roomService.getById(room.getId());
        assertEquals("测试备注", updated.getRemark());

        roomService.removeById(room.getId());
    }

    @Test
    void getAllEmployees() {
        var list = employeeService.list();
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test
    void updateRoomStatus() {
        Room room = roomService.getAllRooms(1, 10).getRecords().get(0);
        Room updated = roomService.updateRoomStatus(room.getId(), "MAINTENANCE");
        assertEquals("MAINTENANCE", updated.getStatus());

        roomService.updateRoomStatus(room.getId(), "AVAILABLE");
    }
}
