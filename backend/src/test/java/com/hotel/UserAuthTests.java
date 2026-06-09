package com.hotel;

import com.hotel.common.exception.AuthException;
import com.hotel.common.exception.ConflictException;
import com.hotel.dto.LoginDTO;
import com.hotel.dto.RegisterDTO;
import com.hotel.entity.SysUser;
import com.hotel.service.SysUserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserAuthTests {

    @Autowired
    private SysUserService sysUserService;

    private static final String TEST_USERNAME = "testuser_01";
    private static final String TEST_PASSWORD = "123456";

    @Test
    @Order(1)
    void register() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(TEST_USERNAME);
        dto.setPassword(TEST_PASSWORD);
        dto.setRealName("测试用户");
        dto.setPhone("13800001111");

        SysUser user = sysUserService.register(dto);

        assertNotNull(user);
        assertEquals(TEST_USERNAME, user.getUsername());
        assertEquals("GUEST", user.getRoleType());
        assertNull(user.getPassword());
    }

    @Test
    @Order(2)
    void registerDuplicate() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(TEST_USERNAME);
        dto.setPassword(TEST_PASSWORD);
        dto.setRealName("测试用户");

        assertThrows(ConflictException.class, () -> sysUserService.register(dto));
    }

    @Test
    @Order(3)
    void loginSuccess() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername(TEST_USERNAME);
        dto.setPassword(TEST_PASSWORD);

        Map<String, Object> result = sysUserService.login(dto);

        assertNotNull(result.get("token"));
        assertNotNull(result.get("user"));
    }

    @Test
    @Order(4)
    void loginFail() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername(TEST_USERNAME);
        dto.setPassword("wrong_password");

        assertThrows(AuthException.class, () -> sysUserService.login(dto));
    }
}
