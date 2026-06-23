package com.dormitory.controller;

import com.dormitory.common.ApiResponse;
import com.dormitory.dto.BedView;
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
@RequestMapping("/api/rooms")
public class RoomController {
    private final DormitoryStructureService structureService;
    private final DormitoryMapService mapService;

    public RoomController(DormitoryStructureService structureService, DormitoryMapService mapService) {
        this.structureService = structureService;
        this.mapService = mapService;
    }

    @PostMapping
    public ApiResponse<Room> create(@RequestBody Room item) {
        return ApiResponse.ok(structureService.createRoom(item));
    }

    @PutMapping("/{id}")
    public ApiResponse<Room> update(@PathVariable Long id, @RequestBody Room item) {
        return ApiResponse.ok(structureService.updateRoom(id, item));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        structureService.deleteRoom(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/beds")
    public ApiResponse<List<BedView>> beds(@PathVariable Long id) {
        return ApiResponse.ok(mapService.getRoomBeds(id));
    }
}
