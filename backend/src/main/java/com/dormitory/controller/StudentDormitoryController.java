package com.dormitory.controller;

import com.dormitory.common.ApiResponse;
import com.dormitory.dto.RoomMapItem;
import com.dormitory.dto.StudentDormitoryView;
import com.dormitory.service.DormitoryMapService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StudentDormitoryController {
    private final DormitoryMapService mapService;

    public StudentDormitoryController(DormitoryMapService mapService) {
        this.mapService = mapService;
    }

    @GetMapping("/student/me/dormitory")
    public ApiResponse<StudentDormitoryView> myDormitory(Authentication authentication) {
        return ApiResponse.ok(mapService.getCurrentStudentDormitory(authentication));
    }

    @GetMapping("/student/me/floor-map")
    public ApiResponse<List<RoomMapItem>> myFloorMap(Authentication authentication) {
        return ApiResponse.ok(mapService.getCurrentStudentFloorMap(authentication));
    }

    @GetMapping("/students/{id}/dormitory")
    public ApiResponse<StudentDormitoryView> studentDormitory(@PathVariable Long id) {
        return ApiResponse.ok(mapService.getStudentDormitory(id));
    }
}
