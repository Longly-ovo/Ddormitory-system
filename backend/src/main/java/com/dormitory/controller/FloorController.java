package com.dormitory.controller;

import com.dormitory.common.ApiResponse;
import com.dormitory.dto.RoomMapItem;
import com.dormitory.entity.Floor;
import com.dormitory.entity.Room;
import com.dormitory.service.DormitoryMapService;
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
@RequestMapping("/api/floors")
public class FloorController {
    private final DormitoryStructureService structureService;
    private final DormitoryMapService mapService;

    public FloorController(DormitoryStructureService structureService, DormitoryMapService mapService) {
        this.structureService = structureService;
        this.mapService = mapService;
    }

    @PostMapping
    public ApiResponse<Floor> create(@RequestBody Floor item) {
        return ApiResponse.ok(structureService.createFloor(item));
    }

    @PutMapping("/{id}")
    public ApiResponse<Floor> update(@PathVariable Long id, @RequestBody Floor item) {
        return ApiResponse.ok(structureService.updateFloor(id, item));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        structureService.deleteFloor(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/rooms")
    public ApiResponse<List<Room>> rooms(@PathVariable Long id) {
        return ApiResponse.ok(structureService.listRoomsByFloor(id));
    }

    @GetMapping("/{id}/map")
    public ApiResponse<List<RoomMapItem>> map(@PathVariable Long id) {
        return ApiResponse.ok(mapService.getFloorMap(id));
    }
}
