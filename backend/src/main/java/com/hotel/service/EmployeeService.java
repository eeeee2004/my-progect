package com.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hotel.dto.LoginDTO;
import com.hotel.entity.Employee;

import java.util.Map;

public interface EmployeeService extends IService<Employee> {
    Map<String, Object> login(LoginDTO loginDTO);
    Employee getCurrentEmployee(Long employeeId);
}
