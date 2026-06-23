package com.dormitory.entity;

import lombok.Data;

@Data
public class Floor {
    private Long id;
    private Long buildingId;
    private Integer floorNo;
    private String name;
}
