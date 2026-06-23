package com.dormitory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dormitory.common.BizException;
import com.dormitory.entity.Bed;
import com.dormitory.mapper.BedMapper;
import com.dormitory.mapper.StudentMapper;
import org.springframework.stereotype.Service;

/*
## Function Index
- createBed() -> 创建床位
- updateBed() -> 修改床位
- deleteBed() -> 删除床位
- assignStudentToBed() -> 床位分配
- releaseBed() -> 退宿
*/
@Service
public class BedService {
    private final BedMapper bedMapper;
    private final StudentMapper studentMapper;

    public BedService(BedMapper bedMapper, StudentMapper studentMapper) {
        this.bedMapper = bedMapper;
        this.studentMapper = studentMapper;
    }

    public Bed createBed(Bed item) {
        if (item.getRoomId() == null || item.getBedNo() == null || item.getBedNo().isBlank()) {
            throw new BizException("寝室和床位号不能为空");
        }
        item.setId(null);
        item.setStudentId(null);
        bedMapper.insert(item);
        return item;
    }

    public Bed updateBed(Long id, Bed item) {
        Bed existing = requireBed(id);
        item.setId(id);
        item.setStudentId(existing.getStudentId());
        bedMapper.updateById(item);
        return bedMapper.selectById(id);
    }

    public void deleteBed(Long id) {
        Bed bed = requireBed(id);
        if (bed.getStudentId() != null) throw new BizException("床位已入住，请先办理退宿");
        bedMapper.deleteById(id);
    }

    public void assignStudentToBed(Long bedId, Long studentId) {
        Bed bed = requireBed(bedId);
        if (bed.getStudentId() != null) throw new BizException("当前床位已入住，请先退宿");
        if (studentMapper.selectById(studentId) == null) throw new BizException("学生不存在");
        Long occupied = bedMapper.selectCount(new LambdaQueryWrapper<Bed>().eq(Bed::getStudentId, studentId));
        if (occupied > 0) throw new BizException("该学生已分配床位，请先退宿");
        bed.setStudentId(studentId);
        bedMapper.updateById(bed);
    }

    public void releaseBed(Long bedId) {
        requireBed(bedId);
        bedMapper.update(null, new UpdateWrapper<Bed>().eq("id", bedId).set("student_id", null));
    }

    private Bed requireBed(Long id) {
        Bed bed = bedMapper.selectById(id);
        if (bed == null) throw new BizException("床位不存在");
        return bed;
    }
}
