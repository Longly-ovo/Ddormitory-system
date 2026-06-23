package com.dormitory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dormitory.auth.ChangePasswordRequest;
import com.dormitory.auth.CurrentUserResponse;
import com.dormitory.auth.JwtService;
import com.dormitory.auth.LoginRequest;
import com.dormitory.auth.LoginResponse;
import com.dormitory.common.BizException;
import com.dormitory.entity.User;
import com.dormitory.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/*
## Function Index
- login() -> 登录逻辑
- getCurrentUser() -> 当前用户信息
- changeStudentPassword() -> 学生修改密码
*/
@Service
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String studentInitialPassword;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtService jwtService,
                       @Value("${app.student.initial-password}") String studentInitialPassword) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.studentInitialPassword = studentInitialPassword;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.username()));
        if (user == null || !Boolean.TRUE.equals(user.getEnabled()) || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        String role = normalizeRole(user);
        boolean mustChangePassword = isStudentUsingInitialPassword(user, role);
        return new LoginResponse(jwtService.createToken(user.getUsername(), role),
                user.getNickname(), role, mustChangePassword);
    }

    public CurrentUserResponse getCurrentUser(Authentication authentication) {
        User user = findEnabledUser(authentication.getName());
        String role = normalizeRole(user);
        boolean mustChangePassword = isStudentUsingInitialPassword(user, role);
        return new CurrentUserResponse(user.getUsername(), user.getNickname(), role, mustChangePassword);
    }

    public void changeStudentPassword(Authentication authentication, ChangePasswordRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, authentication.getName()));
        if (user == null || !"STUDENT".equals(user.getRole())) throw new BizException("学生账号不存在");
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) throw new BizException("旧密码错误");
        if (!request.newPassword().equals(request.confirmPassword())) throw new BizException("两次输入的新密码不一致");
        if (studentInitialPassword.equals(request.newPassword())) throw new BizException("新密码不能继续使用初始密码");
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) throw new BizException("新密码不能与旧密码相同");
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userMapper.updateById(user);
    }

    private User findEnabledUser(String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null || !Boolean.TRUE.equals(user.getEnabled())) throw new BizException("请先登录");
        return user;
    }

    private String normalizeRole(User user) {
        return user.getRole() == null || user.getRole().isBlank() ? "ADMIN" : user.getRole();
    }

    private boolean isStudentUsingInitialPassword(User user, String role) {
        return "STUDENT".equals(role) && passwordEncoder.matches(studentInitialPassword, user.getPassword());
    }
}
