package com.dormitory.entity;

import lombok.Data;

@Data
public class Student {
    private Long id;
    private String studentNo;
    private String name;
    private String gender;
    private String phone;
    private String college;
    private String major;
    private String className;
}
