package com.hotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hotel.dto.LoginDTO;
import com.hotel.dto.RegisterDTO;
import com.hotel.entity.SysUser;

import java.util.Map;

public interface SysUserService extends IService<SysUser> {
    Map<String, Object> login(LoginDTO loginDTO);
    SysUser register(RegisterDTO registerDTO);
    SysUser getCurrentUser(Long userId);
    SysUser updateProfile(Long userId, SysUser user);
}
