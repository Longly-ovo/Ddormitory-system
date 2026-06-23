package com.dormitory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dormitory.common.BizException;
import com.dormitory.entity.Building;
import com.dormitory.entity.Floor;
import com.dormitory.entity.Room;
import com.dormitory.mapper.BuildingMapper;
import com.dormitory.mapper.FloorMapper;
import com.dormitory.mapper.RoomMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/*
## Function Index
- listBuildings() -> 楼栋列表
- createBuilding() -> 创建楼栋
- updateBuilding() -> 修改楼栋
- deleteBuilding() -> 删除楼栋
- listFloorsByBuilding() -> 楼栋下的楼层列表
- createFloor() -> 创建楼层
- updateFloor() -> 修改楼层
- deleteFloor() -> 删除楼层
- listRoomsByFloor() -> 楼层下的房间列表
- createRoom() -> 创建房间
- updateRoom() -> 修改房间
- deleteRoom() -> 删除房间
*/
@Service
public class DormitoryStructureService {
    private final BuildingMapper buildingMapper;
    private final FloorMapper floorMapper;
    private final RoomMapper roomMapper;

    public DormitoryStructureService(BuildingMapper buildingMapper, FloorMapper floorMapper, RoomMapper roomMapper) {
        this.buildingMapper = buildingMapper;
        this.floorMapper = floorMapper;
        this.roomMapper = roomMapper;
    }

    public List<Building> listBuildings() {
        return buildingMapper.selectList(new LambdaQueryWrapper<Building>().orderByAsc(Building::getName));
    }

    public List<Floor> listFloorsByBuilding(Long buildingId) {
        return floorMapper.selectList(new LambdaQueryWrapper<Floor>()
                .eq(Floor::getBuildingId, buildingId).orderByAsc(Floor::getFloorNo));
    }

    public Building createBuilding(Building item) {
        if (item.getName() == null || item.getName().isBlank()) throw new BizException("楼栋名称不能为空");
        item.setId(null);
        buildingMapper.insert(item);
        return item;
    }

    public Building updateBuilding(Long id, Building item) {
        if (buildingMapper.selectById(id) == null) throw new BizException("楼栋不存在");
        item.setId(id);
        buildingMapper.updateById(item);
        return buildingMapper.selectById(id);
    }

    public void deleteBuilding(Long id) {
        buildingMapper.deleteById(id);
    }

    public Floor createFloor(Floor item) {
        if (item.getBuildingId() == null || item.getFloorNo() == null || item.getName() == null || item.getName().isBlank()) {
            throw new BizException("楼栋、楼层号和名称不能为空");
        }
        item.setId(null);
        floorMapper.insert(item);
        return item;
    }

    public Floor updateFloor(Long id, Floor item) {
        if (floorMapper.selectById(id) == null) throw new BizException("楼层不存在");
        item.setId(id);
        floorMapper.updateById(item);
        return floorMapper.selectById(id);
    }

    public void deleteFloor(Long id) {
        floorMapper.deleteById(id);
    }

    public List<Room> listRoomsByFloor(Long floorId) {
        return roomMapper.selectList(new LambdaQueryWrapper<Room>().eq(Room::getFloorId, floorId)
                .orderByAsc(Room::getSortOrder).orderByAsc(Room::getRoomNo));
    }

    public Room createRoom(Room item) {
        if (item.getFloorId() == null || item.getRoomNo() == null || item.getRoomNo().isBlank()) {
            throw new BizException("楼层和寝室号不能为空");
        }
        if (item.getSortOrder() == null) item.setSortOrder(0);
        item.setId(null);
        roomMapper.insert(item);
        return item;
    }

    public Room updateRoom(Long id, Room item) {
        if (roomMapper.selectById(id) == null) throw new BizException("寝室不存在");
        item.setId(id);
        roomMapper.updateById(item);
        return roomMapper.selectById(id);
    }

    public void deleteRoom(Long id) {
        roomMapper.deleteById(id);
    }
}
