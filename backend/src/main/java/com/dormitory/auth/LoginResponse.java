package com.dormitory.auth;

public record LoginResponse(String token, String nickname, String role, boolean mustChangePassword) {}
