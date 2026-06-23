package com.dormitory.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dormitory.entity.*;
import com.dormitory.mapper.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DemoDataInitializer implements CommandLineRunner {
    private final UserMapper users; private final BuildingMapper buildings; private final FloorMapper floors;
    private final RoomMapper rooms; private final BedMapper beds; private final StudentMapper students;
    private final PasswordEncoder encoder;
    @Value("${app.seed-demo-data:true}") private boolean enabled;
    @Value("${app.student.initial-password}") private String studentInitialPassword;

    public DemoDataInitializer(UserMapper users, BuildingMapper buildings, FloorMapper floors, RoomMapper rooms,
                               BedMapper beds, StudentMapper students, PasswordEncoder encoder) {
        this.users = users; this.buildings = buildings; this.floors = floors; this.rooms = rooms;
        this.beds = beds; this.students = students; this.encoder = encoder;
    }

    @Override public void run(String... args) {
        ensureDemoUser("admin", "admin123", "系统管理员", "ADMIN");
        if (!enabled) return;
        ensureDemoUser("dorm", "dorm123", "宿管员", "DORM_MANAGER");
        ensureDemoUser("counselor", "counselor123", "辅导员", "COUNSELOR");
        ensureDemoStudentAccount("20260001");
        ensureDemoStudentAccount("20260002");
        ensureStudentAccounts();
        if (buildings.selectCount(null) > 0) return;
        Building building = new Building(); building.setName("1号宿舍楼"); building.setDescription("MVP 演示楼栋"); buildings.insert(building);
        Floor floor = new Floor(); floor.setBuildingId(building.getId()); floor.setFloorNo(1); floor.setName("一层"); floors.insert(floor);
        Student a = student("20260001", "张明", "男", "计算机学院", "软件工程", "软工2601"); students.insert(a);
        Student b = student("20260002", "李华", "男", "计算机学院", "软件工程", "软工2601"); students.insert(b);
        ensureDemoUser(a.getStudentNo(), studentInitialPassword, a.getName(), "STUDENT");
        ensureDemoUser(b.getStudentNo(), studentInitialPassword, b.getName(), "STUDENT");
        for (int roomIndex = 1; roomIndex <= 8; roomIndex++) {
            Room room = new Room(); room.setFloorId(floor.getId()); room.setRoomNo("10" + roomIndex); room.setSortOrder(roomIndex); rooms.insert(room);
            for (int bedIndex = 1; bedIndex <= 4; bedIndex++) {
                Bed bed = new Bed(); bed.setRoomId(room.getId()); bed.setBedNo(bedIndex + "号床");
                if (roomIndex == 1 && bedIndex == 1) bed.setStudentId(a.getId());
                if (roomIndex == 1 && bedIndex == 2) bed.setStudentId(b.getId());
                beds.insert(bed);
            }
        }
    }

    private void ensureStudentAccounts() {
        students.selectList(null).forEach(student ->
                ensureUser(student.getStudentNo(), studentInitialPassword, student.getName(), "STUDENT"));
    }

    private void ensureDemoStudentAccount(String studentNo) {
        Student student = students.selectOne(new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, studentNo));
        if (student != null) ensureDemoUser(student.getStudentNo(), studentInitialPassword, student.getName(), "STUDENT");
    }

    private Student student(String no, String name, String gender, String college, String major, String clazz) {
        Student s = new Student(); s.setStudentNo(no); s.setName(name); s.setGender(gender);
        s.setPhone("13800000000"); s.setCollege(college); s.setMajor(major); s.setClassName(clazz); return s;
    }

    private User user(String username, String password, String nickname, String role) {
        User user = new User(); user.setUsername(username); user.setPassword(encoder.encode(password));
        user.setNickname(nickname); user.setRole(role); user.setEnabled(true); return user;
    }

    private void ensureUser(String username, String password, String nickname, String role) {
        User existing = users.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (existing == null) {
            users.insert(user(username, password, nickname, role));
            return;
        }
        boolean changed = false;
        if (!role.equals(existing.getRole())) { existing.setRole(role); changed = true; }
        if (!Boolean.TRUE.equals(existing.getEnabled())) { existing.setEnabled(true); changed = true; }
        if (existing.getNickname() == null || existing.getNickname().isBlank()) { existing.setNickname(nickname); changed = true; }
        if (changed) users.updateById(existing);
    }

    private void ensureDemoUser(String username, String password, String nickname, String role) {
        User existing = users.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (existing == null) {
            users.insert(user(username, password, nickname, role));
            return;
        }
        boolean changed = false;
        if (!role.equals(existing.getRole())) { existing.setRole(role); changed = true; }
        if (!Boolean.TRUE.equals(existing.getEnabled())) { existing.setEnabled(true); changed = true; }
        if (nickname != null && !nickname.equals(existing.getNickname())) { existing.setNickname(nickname); changed = true; }
        if (existing.getPassword() == null || !encoder.matches(password, existing.getPassword())) {
            existing.setPassword(encoder.encode(password));
            changed = true;
        }
        if (changed) users.updateById(existing);
    }
}
