package com.dormitory.entity;

import lombok.Data;

@Data
public class Room {
    private Long id;
    private Long floorId;
    private String roomNo;
    private Integer sortOrder;
}
