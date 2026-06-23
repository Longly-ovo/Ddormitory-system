package com.dormitory.dto;

public record StudentListItem(
        Long id,
        String studentNo,
        String name,
        String gender,
        String phone,
        String college,
        String major,
        String className,
        String buildingName,
        String floorName,
        String roomNo,
        String bedNo,
        String dormitoryText
) {
}
