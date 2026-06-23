package com.dormitory.auth;

public record CurrentUserResponse(String username, String nickname, String role, boolean mustChangePassword) {}
