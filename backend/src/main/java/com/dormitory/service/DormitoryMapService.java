package com.dormitory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dormitory.common.BizException;
import com.dormitory.dormitory.OccupancyStatus;
import com.dormitory.dto.BedView;
import com.dormitory.dto.RoomMapItem;
import com.dormitory.dto.StudentDormitoryView;
import com.dormitory.entity.Bed;
import com.dormitory.entity.Building;
import com.dormitory.entity.Floor;
import com.dormitory.entity.Room;
import com.dormitory.entity.Student;
import com.dormitory.mapper.BedMapper;
import com.dormitory.mapper.BuildingMapper;
import com.dormitory.mapper.FloorMapper;
import com.dormitory.mapper.RoomMapper;
import com.dormitory.mapper.StudentMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/*
## Function Index
- getFloorMap() -> 楼层地图
- getRoomBeds() -> 房间床位
- getStudentDormitory() -> 指定学生宿舍信息
- getCurrentStudentDormitory() -> 当前学生宿舍信息
- getCurrentStudentFloorMap() -> 当前学生所在楼层地图
*/
@Service
public class DormitoryMapService {
    private final StudentMapper studentMapper;
    private final BedMapper bedMapper;
    private final RoomMapper roomMapper;
    private final FloorMapper floorMapper;
    private final BuildingMapper buildingMapper;

    public DormitoryMapService(StudentMapper studentMapper, BedMapper bedMapper, RoomMapper roomMapper,
                               FloorMapper floorMapper, BuildingMapper buildingMapper) {
        this.studentMapper = studentMapper;
        this.bedMapper = bedMapper;
        this.roomMapper = roomMapper;
        this.floorMapper = floorMapper;
        this.buildingMapper = buildingMapper;
    }

    public List<RoomMapItem> getFloorMap(Long floorId) {
        List<Room> rooms = roomMapper.selectList(new LambdaQueryWrapper<Room>().eq(Room::getFloorId, floorId)
                .orderByAsc(Room::getSortOrder).orderByAsc(Room::getRoomNo));
        List<RoomMapItem> result = new ArrayList<>();
        for (Room room : rooms) {
            List<Bed> beds = bedMapper.selectList(new LambdaQueryWrapper<Bed>().eq(Bed::getRoomId, room.getId()));
            if (beds.isEmpty()) continue;
            OccupancyStatus.Summary summary = OccupancyStatus.summarize(beds);
            result.add(new RoomMapItem(room.getId(), room.getRoomNo(), room.getSortOrder(), summary.total(),
                    summary.occupied(), summary.empty(), summary.status()));
        }
        return result;
    }

    public List<BedView> getRoomBeds(Long roomId) {
        return bedMapper.selectList(new LambdaQueryWrapper<Bed>().eq(Bed::getRoomId, roomId)
                .orderByAsc(Bed::getBedNo)).stream().map(bed -> {
            Student student = bed.getStudentId() == null ? null : studentMapper.selectById(bed.getStudentId());
            return new BedView(bed.getId(), bed.getBedNo(), bed.getStudentId(),
                    student == null ? null : student.getName(), student == null ? null : student.getStudentNo());
        }).toList();
    }

    public StudentDormitoryView getCurrentStudentDormitory(Authentication authentication) {
        Student student = getCurrentStudent(authentication);
        return buildStudentDormitoryView(student);
    }

    public List<RoomMapItem> getCurrentStudentFloorMap(Authentication authentication) {
        Student student = getCurrentStudent(authentication);
        Bed ownBed = bedMapper.selectOne(new LambdaQueryWrapper<Bed>().eq(Bed::getStudentId, student.getId()));
        if (ownBed == null) return List.of();
        Room ownRoom = roomMapper.selectById(ownBed.getRoomId());
        if (ownRoom == null) return List.of();
        return getFloorMap(ownRoom.getFloorId());
    }

    public StudentDormitoryView getStudentDormitory(Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) throw new BizException("学生不存在");
        return buildStudentDormitoryView(student);
    }

    private Student getCurrentStudent(Authentication authentication) {
        Student student = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getStudentNo, authentication.getName()));
        if (student == null) throw new BizException("未找到与当前账号对应的学生档案");
        return student;
    }

    private StudentDormitoryView buildStudentDormitoryView(Student student) {
        Bed bed = bedMapper.selectOne(new LambdaQueryWrapper<Bed>().eq(Bed::getStudentId, student.getId()));
        if (bed == null) return new StudentDormitoryView(student, null, null, null, null);
        Room room = roomMapper.selectById(bed.getRoomId());
        Floor floor = room == null ? null : floorMapper.selectById(room.getFloorId());
        Building building = floor == null ? null : buildingMapper.selectById(floor.getBuildingId());
        return new StudentDormitoryView(student, building, floor, room, bed);
    }
}
