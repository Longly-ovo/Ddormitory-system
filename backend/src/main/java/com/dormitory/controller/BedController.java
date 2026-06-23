package com.dormitory.controller;

import com.dormitory.common.ApiResponse;
import com.dormitory.dto.BedStudentRequest;
import com.dormitory.entity.Bed;
import com.dormitory.service.BedService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/beds")
public class BedController {
    private final BedService bedService;

    public BedController(BedService bedService) {
        this.bedService = bedService;
    }

    @PostMapping
    public ApiResponse<Bed> create(@RequestBody Bed item) {
        return ApiResponse.ok(bedService.createBed(item));
    }

    @PutMapping("/{id}")
    public ApiResponse<Bed> update(@PathVariable Long id, @RequestBody Bed item) {
        return ApiResponse.ok(bedService.updateBed(id, item));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bedService.deleteBed(id);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/student")
    public ApiResponse<Void> assign(@PathVariable Long id, @Valid @RequestBody BedStudentRequest request) {
        bedService.assignStudentToBed(id, request.studentId());
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}/student")
    public ApiResponse<Void> checkout(@PathVariable Long id) {
        bedService.releaseBed(id);
        return ApiResponse.ok();
    }
}
