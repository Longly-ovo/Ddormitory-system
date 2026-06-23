package com.dormitory.dto;

import com.dormitory.entity.Bed;
import com.dormitory.entity.Building;
import com.dormitory.entity.Floor;
import com.dormitory.entity.Room;
import com.dormitory.entity.Student;

public record StudentDormitoryView(Student student, Building building, Floor floor, Room room, Bed bed) {}
