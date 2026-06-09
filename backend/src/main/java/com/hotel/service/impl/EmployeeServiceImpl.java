package com.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hotel.common.JwtUtils;
import com.hotel.common.exception.AuthException;
import com.hotel.dto.LoginDTO;
import com.hotel.entity.Employee;
import com.hotel.mapper.EmployeeMapper;
import com.hotel.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        Employee employee = this.getOne(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getUsername, loginDTO.getUsername()));
        if (employee == null) {
            throw new AuthException("用户名或密码错误");
        }
        if (employee.getStatus() == 0) {
            throw new AuthException("账号已被禁用");
        }
        if (!passwordEncoder.matches(loginDTO.getPassword(), employee.getPassword())) {
            throw new AuthException("用户名或密码错误");
        }
        String token = jwtUtils.generateEmployeeToken(employee.getId(), employee.getUsername(), employee.getRoleType());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        employee.setPassword(null);
        result.put("employee", employee);
        return result;
    }

    @Override
    public Employee getCurrentEmployee(Long employeeId) {
        Employee employee = this.getById(employeeId);
        if (employee != null) {
            employee.setPassword(null);
        }
        return employee;
    }
}
