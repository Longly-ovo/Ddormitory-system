package com.dormitory.controller;

import com.dormitory.common.ApiResponse;
import com.dormitory.dto.StudentListItem;
import com.dormitory.entity.Student;
import com.dormitory.service.StudentService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ApiResponse<List<StudentListItem>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(studentService.listStudentRows(keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<Student> detail(@PathVariable Long id) {
        return ApiResponse.ok(studentService.getStudentDetail(id));
    }

    @PostMapping
    public ApiResponse<Student> create(@RequestBody Student item) {
        return ApiResponse.ok(studentService.createStudentAccount(item));
    }

    @PutMapping("/{id}")
    public ApiResponse<Student> update(@PathVariable Long id, @RequestBody Student item) {
        return ApiResponse.ok(studentService.updateStudentProfile(id, item));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        studentService.deleteStudentAndAccount(id);
        return ApiResponse.ok();
    }
}
