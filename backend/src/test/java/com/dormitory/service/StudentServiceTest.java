package com.dormitory.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.dormitory.common.BizException;
import com.dormitory.entity.Student;
import com.dormitory.entity.User;
import com.dormitory.mapper.BedMapper;
import com.dormitory.mapper.BuildingMapper;
import com.dormitory.mapper.FloorMapper;
import com.dormitory.mapper.RoomMapper;
import com.dormitory.mapper.StudentMapper;
import com.dormitory.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudentServiceTest {
    private StudentMapper students;
    private BedMapper beds;
    private RoomMapper rooms;
    private FloorMapper floors;
    private BuildingMapper buildings;
    private UserMapper users;
    private PasswordEncoder encoder;
    private StudentService service;

    @BeforeEach
    void setUp() {
        students = mock(StudentMapper.class);
        beds = mock(BedMapper.class);
        rooms = mock(RoomMapper.class);
        floors = mock(FloorMapper.class);
        buildings = mock(BuildingMapper.class);
        users = mock(UserMapper.class);
        encoder = mock(PasswordEncoder.class);
        service = new StudentService(students, beds, rooms, floors, buildings, users, encoder, "123456");
    }

    @Test
    void createsStudentAndStudentAccountTogether() {
        when(students.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(users.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(encoder.encode("123456")).thenReturn("encoded-password");
        Student student = student("20269999", "测试学生");

        assertEquals(student, service.createStudentAccount(student));
        verify(students).insert(student);
        ArgumentCaptor<User> account = ArgumentCaptor.forClass(User.class);
        verify(users).insert(account.capture());
        assertEquals("20269999", account.getValue().getUsername());
        assertEquals("测试学生", account.getValue().getNickname());
        assertEquals("STUDENT", account.getValue().getRole());
        assertEquals("encoded-password", account.getValue().getPassword());
        assertTrue(account.getValue().getEnabled());
    }

    @Test
    void rejectsExistingStudentNumberBeforeWriting() {
        when(students.selectCount(any(Wrapper.class))).thenReturn(1L);

        assertThrows(BizException.class, () -> service.createStudentAccount(student("20269999", "测试学生")));
        verify(students, never()).insert(any(Student.class));
        verify(users, never()).insert(any(User.class));
    }

    @Test
    void preventsChangingStudentNumber() {
        when(students.selectById(1L)).thenReturn(student("20260001", "张明"));

        assertThrows(BizException.class, () -> service.updateStudentProfile(1L, student("20260009", "张明")));
        verify(students, never()).updateById(any(Student.class));
    }

    @Test
    void deletesMatchingStudentAccountWhenStudentIsDeleted() {
        when(students.selectById(1L)).thenReturn(student("20269999", "测试学生"));
        when(beds.selectCount(any(Wrapper.class))).thenReturn(0L);

        service.deleteStudentAndAccount(1L);
        verify(students).deleteById(1L);
        verify(users).delete(any(Wrapper.class));
    }

    private Student student(String no, String name) {
        Student student = new Student();
        student.setStudentNo(no);
        student.setName(name);
        return student;
    }
}
