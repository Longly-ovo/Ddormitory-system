package com.dormitory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dormitory.common.BizException;
import com.dormitory.dto.StudentListItem;
import com.dormitory.entity.Bed;
import com.dormitory.entity.Building;
import com.dormitory.entity.Floor;
import com.dormitory.entity.Room;
import com.dormitory.entity.Student;
import com.dormitory.entity.User;
import com.dormitory.mapper.BedMapper;
import com.dormitory.mapper.BuildingMapper;
import com.dormitory.mapper.FloorMapper;
import com.dormitory.mapper.RoomMapper;
import com.dormitory.mapper.StudentMapper;
import com.dormitory.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/*
## Function Index
- listStudents() -> 学生列表与搜索
- listStudentRows() -> 学生列表与当前住宿位置
- getStudentDetail() -> 学生详情
- createStudentAccount() -> 创建学生并自动开户
- updateStudentProfile() -> 修改学生信息
- deleteStudentAndAccount() -> 删除学生及对应账号
*/
@Service
public class StudentService {
    private final StudentMapper studentMapper;
    private final BedMapper bedMapper;
    private final RoomMapper roomMapper;
    private final FloorMapper floorMapper;
    private final BuildingMapper buildingMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final String studentInitialPassword;

    public StudentService(StudentMapper studentMapper, BedMapper bedMapper, RoomMapper roomMapper,
                          FloorMapper floorMapper, BuildingMapper buildingMapper, UserMapper userMapper,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.student.initial-password}") String studentInitialPassword) {
        this.studentMapper = studentMapper;
        this.bedMapper = bedMapper;
        this.roomMapper = roomMapper;
        this.floorMapper = floorMapper;
        this.buildingMapper = buildingMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.studentInitialPassword = studentInitialPassword;
    }

    public List<Student> listStudents(String keyword) {
        LambdaQueryWrapper<Student> query = new LambdaQueryWrapper<Student>().orderByAsc(Student::getStudentNo);
        if (StringUtils.hasText(keyword)) {
            query.and(q -> q.like(Student::getStudentNo, keyword).or().like(Student::getName, keyword));
        }
        return studentMapper.selectList(query);
    }

    public List<StudentListItem> listStudentRows(String keyword) {
        String trimmedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        List<StudentListItem> rows = listStudents(null).stream().map(this::buildStudentListItem).toList();
        if (trimmedKeyword == null) return rows;
        return rows.stream().filter(row -> matchesStudentKeyword(row, trimmedKeyword)).toList();
    }

    public Student getStudentDetail(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) throw new BizException("学生不存在");
        return student;
    }

    @Transactional
    public Student createStudentAccount(Student item) {
        validateStudentProfile(item);
        if (studentMapper.selectCount(new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, item.getStudentNo())) > 0) {
            throw new BizException("学号已存在");
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, item.getStudentNo())) > 0) {
            throw new BizException("该学号对应的登录账号已存在");
        }
        item.setId(null);
        studentMapper.insert(item);

        User user = new User();
        user.setUsername(item.getStudentNo());
        user.setNickname(item.getName());
        user.setPassword(passwordEncoder.encode(studentInitialPassword));
        user.setRole("STUDENT");
        user.setEnabled(true);
        userMapper.insert(user);
        return item;
    }

    public Student updateStudentProfile(Long id, Student item) {
        Student existing = getStudentDetail(id);
        validateStudentProfile(item);
        if (!existing.getStudentNo().equals(item.getStudentNo())) throw new BizException("学号创建后不可修改");
        item.setId(id);
        studentMapper.updateById(item);
        return studentMapper.selectById(id);
    }

    @Transactional
    public void deleteStudentAndAccount(Long id) {
        Student student = getStudentDetail(id);
        if (bedMapper.selectCount(new LambdaQueryWrapper<Bed>().eq(Bed::getStudentId, id)) > 0) {
            throw new BizException("学生仍占用床位，请先办理退宿");
        }
        studentMapper.deleteById(id);
        userMapper.delete(new LambdaQueryWrapper<User>().eq(User::getUsername, student.getStudentNo())
                .eq(User::getRole, "STUDENT"));
    }

    private void validateStudentProfile(Student item) {
        if (!StringUtils.hasText(item.getStudentNo()) || !StringUtils.hasText(item.getName())) {
            throw new BizException("学号和姓名不能为空");
        }
    }

    private StudentListItem buildStudentListItem(Student student) {
        Bed bed = bedMapper.selectOne(new LambdaQueryWrapper<Bed>().eq(Bed::getStudentId, student.getId()));
        if (bed == null) return toListItem(student, null, null, null, null);
        Room room = roomMapper.selectById(bed.getRoomId());
        Floor floor = room == null ? null : floorMapper.selectById(room.getFloorId());
        Building building = floor == null ? null : buildingMapper.selectById(floor.getBuildingId());
        return toListItem(student, building, floor, room, bed);
    }

    private StudentListItem toListItem(Student student, Building building, Floor floor, Room room, Bed bed) {
        String dormitoryText = "未分配";
        if (bed != null && building != null && floor != null && room != null) {
            dormitoryText = building.getName() + " / " + room.getRoomNo();
        }
        return new StudentListItem(student.getId(), student.getStudentNo(), student.getName(), student.getGender(),
                student.getPhone(), student.getCollege(), student.getMajor(), student.getClassName(),
                building == null ? null : building.getName(), floor == null ? null : floor.getName(),
                room == null ? null : room.getRoomNo(), bed == null ? null : bed.getBedNo(), dormitoryText);
    }

    private boolean matchesStudentKeyword(StudentListItem row, String keyword) {
        return contains(row.studentNo(), keyword)
                || contains(row.name(), keyword)
                || contains(row.gender(), keyword)
                || contains(row.college(), keyword)
                || contains(row.major(), keyword)
                || contains(row.className(), keyword)
                || contains(row.buildingName(), keyword)
                || contains(row.floorName(), keyword)
                || contains(row.roomNo(), keyword)
                || contains(row.bedNo(), keyword)
                || contains(row.dormitoryText(), keyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.contains(keyword);
    }
}
