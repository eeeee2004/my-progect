package com.hotel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.common.PageResult;
import com.hotel.common.Result;
import com.hotel.entity.BookingOrder;
import com.hotel.entity.Employee;
import com.hotel.entity.OperationLog;
import com.hotel.entity.PriceCalendar;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.mapper.BookingOrderMapper;
import com.hotel.mapper.OperationLogMapper;
import com.hotel.mapper.RoomMapper;
import com.hotel.mapper.RoomTypeMapper;
import com.hotel.service.EmployeeService;
import com.hotel.service.PriceCalendarService;
import com.hotel.service.RoomService;
import com.hotel.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RoomTypeService roomTypeService;
    private final RoomService roomService;
    private final EmployeeService employeeService;
    private final RoomTypeMapper roomTypeMapper;
    private final RoomMapper roomMapper;
    private final BookingOrderMapper bookingOrderMapper;
    private final OperationLogMapper operationLogMapper;
    private final PriceCalendarService priceCalendarService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/room-types")
    public Result<List<RoomType>> getAllRoomTypes() {
        return Result.success(roomTypeService.list());
    }

    @PostMapping("/room-types")
    @com.hotel.common.annotation.OperationLog(module = "系统管理", action = "新增房型", description = "管理员新增房型")
    public Result<RoomType> createRoomType(@RequestBody RoomType roomType) {
        roomTypeService.save(roomType);
        return Result.success("房型创建成功", roomType);
    }

    @PutMapping("/room-types/{id}")
    @com.hotel.common.annotation.OperationLog(module = "系统管理", action = "修改房型", description = "管理员修改房型信息")
    public Result<RoomType> updateRoomType(@PathVariable Long id, @RequestBody RoomType roomType) {
        roomType.setId(id);
        roomTypeService.updateById(roomType);
        return Result.success("房型更新成功", roomType);
    }

    @DeleteMapping("/room-types/{id}")
    @com.hotel.common.annotation.OperationLog(module = "系统管理", action = "删除房型", description = "管理员删除房型")
    public Result<Void> deleteRoomType(@PathVariable Long id) {
        roomTypeService.removeById(id);
        return Result.success("房型删除成功");
    }

    @PostMapping("/rooms")
    public Result<Room> createRoom(@RequestBody Room room) {
        roomService.save(room);
        return Result.success("房间创建成功", room);
    }

    @PutMapping("/rooms/{id}")
    public Result<Room> updateRoom(@PathVariable Long id, @RequestBody Room room) {
        room.setId(id);
        roomService.updateById(room);
        return Result.success("房间更新成功", room);
    }

    @DeleteMapping("/rooms/{id}")
    public Result<Void> deleteRoom(@PathVariable Long id) {
        roomService.removeById(id);
        return Result.success("房间删除成功");
    }

    @GetMapping("/employees")
    public Result<List<Employee>> getAllEmployees() {
        return Result.success(employeeService.list());
    }

    @PostMapping("/employees")
    @com.hotel.common.annotation.OperationLog(module = "员工管理", action = "新增员工", description = "管理员新增员工账号")
    public Result<Employee> createEmployee(@RequestBody Employee employee) {
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        employee.setStatus(1);
        employeeService.save(employee);
        employee.setPassword(null);
        return Result.success("员工添加成功", employee);
    }

    @PutMapping("/employees/{id}")
    public Result<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        Employee existing = employeeService.getById(id);
        if (existing == null) {
            return Result.error("员工不存在");
        }
        if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(employee.getPassword()));
        }
        if (employee.getRealName() != null) existing.setRealName(employee.getRealName());
        if (employee.getPhone() != null) existing.setPhone(employee.getPhone());
        if (employee.getEmail() != null) existing.setEmail(employee.getEmail());
        if (employee.getRoleType() != null) existing.setRoleType(employee.getRoleType());
        if (employee.getStatus() != null) existing.setStatus(employee.getStatus());
        employeeService.updateById(existing);
        existing.setPassword(null);
        return Result.success("员工更新成功", existing);
    }

    @DeleteMapping("/employees/{id}")
    @com.hotel.common.annotation.OperationLog(module = "员工管理", action = "删除员工", description = "管理员删除员工账号")
    public Result<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.removeById(id);
        return Result.success("员工删除成功");
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam(defaultValue = "daily") String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> stats = new HashMap<>();

        long totalRooms = roomMapper.selectCount(null);
        long occupiedRooms = roomMapper.selectCount(new LambdaQueryWrapper<Room>().eq(Room::getStatus, "OCCUPIED"));
        stats.put("totalRooms", totalRooms);
        stats.put("occupiedRooms", occupiedRooms);
        stats.put("availableRooms", totalRooms - occupiedRooms);
        stats.put("occupancyRate", totalRooms > 0 ?
            BigDecimal.valueOf(occupiedRooms).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalRooms), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO);

        var orderWrapper = new LambdaQueryWrapper<BookingOrder>();
        if (startDate != null && !startDate.isEmpty()) {
            orderWrapper.ge(BookingOrder::getCreateTime, LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE).atStartOfDay());
        }
        if (endDate != null && !endDate.isEmpty()) {
            orderWrapper.le(BookingOrder::getCreateTime, LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE).atTime(23, 59, 59));
        }
        if (startDate == null && endDate == null) {
            orderWrapper.ge(BookingOrder::getCreateTime, LocalDate.now().atStartOfDay());
        }

        List<BookingOrder> orders = bookingOrderMapper.selectList(orderWrapper);
        stats.put("totalOrders", orders.size());
        stats.put("completedOrders", orders.stream().filter(o -> "COMPLETED".equals(o.getStatus())).count());
        stats.put("pendingOrders", orders.stream().filter(o -> "PENDING".equals(o.getStatus())).count());
        stats.put("totalRevenue", orders.stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()))
                .map(BookingOrder::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        return Result.success(stats);
    }

    @GetMapping("/statistics/trend")
    public Result<Map<String, Object>> getTrendStatistics(
            @RequestParam(defaultValue = "7") int days) {
        Map<String, Object> result = new HashMap<>();

        long totalRooms = roomMapper.selectCount(null);
        List<Map<String, Object>> occupancyTrend = new ArrayList<>();
        List<Map<String, Object>> revenueTrend = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDate nextDate = date.plusDays(1);

            long occupied = roomMapper.selectCount(new LambdaQueryWrapper<Room>()
                    .eq(Room::getStatus, "OCCUPIED"));
            BigDecimal rate = totalRooms > 0
                ? BigDecimal.valueOf(occupied).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalRooms), 1, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("rate", rate);
            occupancyTrend.add(dayData);

            List<BookingOrder> dayOrders = bookingOrderMapper.selectList(
                new LambdaQueryWrapper<BookingOrder>()
                    .ge(BookingOrder::getCreateTime, date.atStartOfDay())
                    .lt(BookingOrder::getCreateTime, nextDate.atStartOfDay())
                    .eq(BookingOrder::getStatus, "COMPLETED"));
            BigDecimal dayRevenue = dayOrders.stream()
                    .map(BookingOrder::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> revData = new HashMap<>();
            revData.put("date", date.toString());
            revData.put("amount", dayRevenue);
            revenueTrend.add(revData);
        }

        List<Map<String, Object>> roomTypeRevenue = new ArrayList<>();
        List<RoomType> types = roomTypeMapper.selectList(null);
        for (RoomType rt : types) {
            var rooms = roomMapper.selectList(new LambdaQueryWrapper<Room>().eq(Room::getRoomTypeId, rt.getId()));
            var roomIds = rooms.stream().map(Room::getId).collect(java.util.stream.Collectors.toList());
            if (roomIds.isEmpty()) continue;
            BigDecimal typeRevenue = BigDecimal.ZERO;
            if (!roomIds.isEmpty()) {
                var orders = bookingOrderMapper.selectList(new LambdaQueryWrapper<BookingOrder>()
                        .in(BookingOrder::getRoomId, roomIds)
                        .eq(BookingOrder::getStatus, "COMPLETED"));
                typeRevenue = orders.stream()
                        .map(BookingOrder::getTotalAmount)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            Map<String, Object> tr = new HashMap<>();
            tr.put("name", rt.getName());
            tr.put("revenue", typeRevenue);
            roomTypeRevenue.add(tr);
        }

        result.put("occupancyTrend", occupancyTrend);
        result.put("revenueTrend", revenueTrend);
        result.put("roomTypeRevenue", roomTypeRevenue);
        return Result.success(result);
    }

    @GetMapping("/rooms")
    public Result<PageResult<Room>> getAllRooms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        var result = roomService.getAllRooms(page, pageSize);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @GetMapping("/orders")
    public Result<PageResult<BookingOrder>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        var wrapper = new LambdaQueryWrapper<BookingOrder>().orderByDesc(BookingOrder::getCreateTime);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(BookingOrder::getStatus, status);
        }
        var result = bookingOrderMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @GetMapping("/logs")
    public Result<PageResult<OperationLog>> getOperationLogs(
            @RequestParam(required = false) String module,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        var wrapper = new LambdaQueryWrapper<OperationLog>().orderByDesc(OperationLog::getCreateTime);
        if (module != null && !module.isEmpty()) {
            wrapper.eq(OperationLog::getModule, module);
        }
        var result = operationLogMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @GetMapping("/price-calendar")
    public Result<PageResult<PriceCalendar>> getPriceCalendar(
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        var wrapper = new LambdaQueryWrapper<PriceCalendar>().orderByDesc(PriceCalendar::getStartDate);
        if (roomTypeId != null) {
            wrapper.eq(PriceCalendar::getRoomTypeId, roomTypeId);
        }
        var result = priceCalendarService.page(new Page<>(page, pageSize), wrapper);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @PostMapping("/price-calendar")
    @com.hotel.common.annotation.OperationLog(module = "系统管理", action = "新增房价规则", description = "管理员新增房价日历规则")
    public Result<PriceCalendar> createPriceRule(@RequestBody PriceCalendar rule) {
        priceCalendarService.save(rule);
        return Result.success("房价规则新增成功", rule);
    }

    @PutMapping("/price-calendar/{id}")
    @com.hotel.common.annotation.OperationLog(module = "系统管理", action = "修改房价规则", description = "管理员修改房价日历规则")
    public Result<PriceCalendar> updatePriceRule(@PathVariable Long id, @RequestBody PriceCalendar rule) {
        rule.setId(id);
        priceCalendarService.updateById(rule);
        return Result.success("房价规则更新成功", rule);
    }

    @DeleteMapping("/price-calendar/{id}")
    @com.hotel.common.annotation.OperationLog(module = "系统管理", action = "删除房价规则", description = "管理员删除房价日历规则")
    public Result<Void> deletePriceRule(@PathVariable Long id) {
        priceCalendarService.removeById(id);
        return Result.success("房价规则删除成功");
    }
}
