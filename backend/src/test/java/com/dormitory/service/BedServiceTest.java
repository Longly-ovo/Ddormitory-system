package com.dormitory.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.dormitory.common.BizException;
import com.dormitory.entity.Bed;
import com.dormitory.entity.Student;
import com.dormitory.mapper.BedMapper;
import com.dormitory.mapper.StudentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BedServiceTest {
    private BedMapper beds;
    private StudentMapper students;
    private BedService service;

    @BeforeEach
    void setUp() {
        beds = mock(BedMapper.class);
        students = mock(StudentMapper.class);
        service = new BedService(beds, students);
    }

    @Test
    void assignsStudentToEmptyBed() {
        Bed bed = bed(1L, null);
        when(beds.selectById(1L)).thenReturn(bed);
        when(students.selectById(2L)).thenReturn(new Student());
        when(beds.selectCount(any(Wrapper.class))).thenReturn(0L);

        service.assignStudentToBed(1L, 2L);

        verify(beds).updateById(bed);
    }

    @Test
    void rejectsAssigningOccupiedBed() {
        when(beds.selectById(1L)).thenReturn(bed(1L, 9L));

        assertThrows(BizException.class, () -> service.assignStudentToBed(1L, 2L));
    }

    @Test
    void releasesExistingBed() {
        when(beds.selectById(1L)).thenReturn(bed(1L, 2L));

        service.releaseBed(1L);

        verify(beds).update(isNull(), any(Wrapper.class));
    }

    private Bed bed(Long id, Long studentId) {
        Bed bed = new Bed();
        bed.setId(id);
        bed.setRoomId(1L);
        bed.setBedNo("1");
        bed.setStudentId(studentId);
        return bed;
    }
}
