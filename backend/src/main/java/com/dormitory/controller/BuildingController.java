package com.dormitory.controller;

import com.dormitory.common.ApiResponse;
import com.dormitory.entity.Building;
import com.dormitory.entity.Floor;
import com.dormitory.service.DormitoryStructureService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/buildings")
public class BuildingController {
    private final DormitoryStructureService structureService;

    public BuildingController(DormitoryStructureService structureService) {
        this.structureService = structureService;
    }

    @GetMapping
    public ApiResponse<List<Building>> list() {
        return ApiResponse.ok(structureService.listBuildings());
    }

    @GetMapping("/{id}/floors")
    public ApiResponse<List<Floor>> floors(@PathVariable Long id) {
        return ApiResponse.ok(structureService.listFloorsByBuilding(id));
    }

    @PostMapping
    public ApiResponse<Building> create(@RequestBody Building item) {
        return ApiResponse.ok(structureService.createBuilding(item));
    }

    @PutMapping("/{id}")
    public ApiResponse<Building> update(@PathVariable Long id, @RequestBody Building item) {
        return ApiResponse.ok(structureService.updateBuilding(id, item));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        structureService.deleteBuilding(id);
        return ApiResponse.ok();
    }
}
