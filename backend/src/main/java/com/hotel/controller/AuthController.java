package com.hotel.controller;

import com.hotel.common.LoginRateLimiter;
import com.hotel.common.Result;
import com.hotel.dto.LoginDTO;
import com.hotel.dto.RegisterDTO;
import com.hotel.service.EmployeeService;
import com.hotel.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final EmployeeService employeeService;
    private final LoginRateLimiter rateLimiter;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        String rateKey = clientIp + ":" + loginDTO.getUsername();
        LoginRateLimiter.RateLimitResult check = rateLimiter.check(rateKey);
        if (!check.allowed()) {
            Map<String, Object> locked = new HashMap<>();
            locked.put("locked", true);
            locked.put("remainingSeconds", check.remainingSeconds());
            locked.put("message", check.message());
            return Result.error(429, check.message());
        }

        boolean isEmployee = "EMPLOYEE".equals(loginDTO.getLoginType());
        Map<String, Object> loginResult;
        try {
            loginResult = isEmployee ? employeeService.login(loginDTO) : sysUserService.login(loginDTO);
            rateLimiter.clear(rateKey);
            return Result.success(loginResult);
        } catch (Exception e) {
            rateLimiter.recordFailure(rateKey);
            int remaining = rateLimiter.getRemainingAttempts(rateKey);
            String msg = e.getMessage();
            if (remaining > 0) {
                msg += "（剩余尝试次数: " + remaining + "）";
            }
            return Result.error(msg);
        }
    }

    @GetMapping("/login-status")
    public Result<Map<String, Object>> loginStatus(
            @RequestParam String username, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        String rateKey = clientIp + ":" + username;
        LoginRateLimiter.RateLimitResult check = rateLimiter.check(rateKey);
        int remaining = rateLimiter.getRemainingAttempts(rateKey);
        Map<String, Object> status = new HashMap<>();
        status.put("locked", !check.allowed());
        status.put("remainingSeconds", check.remainingSeconds());
        status.put("remainingAttempts", remaining);
        status.put("maxAttempts", 5);
        return Result.success(status);
    }

    @PostMapping("/register")
    public Result<Object> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return Result.success("注册成功", sysUserService.register(registerDTO));
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
