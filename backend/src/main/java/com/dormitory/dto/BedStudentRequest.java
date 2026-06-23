package com.dormitory.dto;

import jakarta.validation.constraints.NotNull;

public record BedStudentRequest(@NotNull(message = "请选择学生") Long studentId) {}
