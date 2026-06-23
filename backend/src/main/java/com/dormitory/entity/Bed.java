package com.dormitory.entity;

import lombok.Data;

@Data
public class Bed {
    private Long id;
    private Long roomId;
    private String bedNo;
    private Long studentId;
}
