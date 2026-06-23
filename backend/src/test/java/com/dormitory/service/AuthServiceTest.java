package com.dormitory.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.dormitory.auth.ChangePasswordRequest;
import com.dormitory.auth.JwtService;
import com.dormitory.auth.LoginRequest;
import com.dormitory.auth.LoginResponse;
import com.dormitory.common.BizException;
import com.dormitory.entity.User;
import com.dormitory.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {
    private UserMapper users;
    private PasswordEncoder encoder;
    private JwtService jwt;
    private AuthService service;

    @BeforeEach
    void setUp() {
        users = mock(UserMapper.class);
        encoder = mock(PasswordEncoder.class);
        jwt = mock(JwtService.class);
        service = new AuthService(users, encoder, jwt, "123456");
    }

    @Test
    void reportsInitialPasswordForStudentLogin() {
        User user = studentUser("hash");
        when(users.selectOne(any(Wrapper.class))).thenReturn(user);
        when(encoder.matches("123456", "hash")).thenReturn(true);
        when(jwt.createToken("20269999", "STUDENT")).thenReturn("token");

        LoginResponse result = service.login(new LoginRequest("20269999", "123456"));

        assertTrue(result.mustChangePassword());
        assertEquals("STUDENT", result.role());
    }

    @Test
    void changesPasswordWhenOldPasswordIsCorrect() {
        User user = studentUser("old-hash");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("20269999");
        when(users.selectOne(any(Wrapper.class))).thenReturn(user);
        when(encoder.matches("old-password", "old-hash")).thenReturn(true);
        when(encoder.matches("new-password", "old-hash")).thenReturn(false);
        when(encoder.encode("new-password")).thenReturn("new-hash");

        service.changeStudentPassword(authentication,
                new ChangePasswordRequest("old-password", "new-password", "new-password"));
        assertEquals("new-hash", user.getPassword());
        verify(users).updateById(user);
    }

    @Test
    void rejectsReusingInitialPassword() {
        User user = studentUser("old-hash");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("20269999");
        when(users.selectOne(any(Wrapper.class))).thenReturn(user);
        when(encoder.matches(eq("old-password"), eq("old-hash"))).thenReturn(true);

        assertThrows(BizException.class, () -> service.changeStudentPassword(authentication,
                new ChangePasswordRequest("old-password", "123456", "123456")));
        verify(users, never()).updateById(any(User.class));
    }

    private User studentUser(String password) {
        User user = new User();
        user.setUsername("20269999");
        user.setNickname("测试学生");
        user.setPassword(password);
        user.setRole("STUDENT");
        user.setEnabled(true);
        return user;
    }
}
