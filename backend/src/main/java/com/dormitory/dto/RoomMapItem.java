package com.dormitory.dto;

public record RoomMapItem(Long id, String roomNo, Integer sortOrder, long totalBeds,
                          long occupiedBeds, long emptyBeds, String status) {}
