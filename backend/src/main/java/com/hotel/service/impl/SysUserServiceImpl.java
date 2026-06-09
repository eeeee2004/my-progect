package com.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hotel.common.JwtUtils;
import com.hotel.common.annotation.OperationLog;
import com.hotel.common.exception.AuthException;
import com.hotel.common.exception.ConflictException;
import com.hotel.common.exception.NotFoundException;
import com.hotel.dto.LoginDTO;
import com.hotel.dto.RegisterDTO;
import com.hotel.entity.SysUser;
import com.hotel.mapper.SysUserMapper;
import com.hotel.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        SysUser user = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginDTO.getUsername()));
        if (user == null) {
            throw new AuthException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new AuthException("账号已被禁用");
        }
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new AuthException("用户名或密码错误");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRoleType());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        user.setPassword(null);
        result.put("user", user);
        return result;
    }

    @Override
    @OperationLog(module = "用户管理", action = "用户注册", description = "新住客注册账号")
    public SysUser register(RegisterDTO registerDTO) {
        long count = this.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, registerDTO.getUsername()));
        if (count > 0) {
            throw new ConflictException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRealName(registerDTO.getRealName());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setIdCard(registerDTO.getIdCard());
        user.setRoleType("GUEST");
        user.setStatus(1);
        this.save(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public SysUser getCurrentUser(Long userId) {
        SysUser user = this.getById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @Override
    public SysUser updateProfile(Long userId, SysUser updateUser) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new NotFoundException("用户", userId);
        }
        if (updateUser.getRealName() != null) {
            user.setRealName(updateUser.getRealName());
        }
        if (updateUser.getPhone() != null) {
            user.setPhone(updateUser.getPhone());
        }
        if (updateUser.getEmail() != null) {
            user.setEmail(updateUser.getEmail());
        }
        if (updateUser.getIdCard() != null) {
            user.setIdCard(updateUser.getIdCard());
        }
        this.updateById(user);
        user.setPassword(null);
        return user;
    }
}
