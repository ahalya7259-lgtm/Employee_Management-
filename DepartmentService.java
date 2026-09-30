package com.employeehub.department.service;

import com.employeehub.department.dto.DepartmentRequest;
import com.employeehub.department.dto.DepartmentResponse;
import com.employeehub.department.entity.Department;
import com.employeehub.department.repository.DepartmentRepository;
import com.employeehub.employee.entity.Employee;
import com.employeehub.employee.repository.EmployeeRepository;
import com.employeehub.exception.ConflictException;
import com.employeehub.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        Department dept = departmentRepository.findByIdWithHead(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
        return toResponse(dept);
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new ConflictException("Department name already exists");
        }
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Department code already exists");
        }
        Department dept = Department.builder()
                .name(request.getName())
                .code(request.getCode().toUpperCase())
                .description(request.getDescription())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        if (request.getHeadId() != null) {
            Employee head = employeeRepository.findByIdActive(request.getHeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Head employee not found"));
            dept.setHead(head);
        }
        return toResponse(departmentRepository.save(dept));
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
        dept.setName(request.getName());
        dept.setCode(request.getCode().toUpperCase());
        dept.setDescription(request.getDescription());
        if (request.getActive() != null) {
            dept.setActive(request.getActive());
        }
        if (request.getHeadId() != null) {
            Employee head = employeeRepository.findByIdActive(request.getHeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Head employee not found"));
            dept.setHead(head);
        } else {
            dept.setHead(null);
        }
        return toResponse(departmentRepository.save(dept));
    }

    private DepartmentResponse toResponse(Department d) {
        long count = employeeRepository.countByDepartmentId(d.getId());
        return DepartmentResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .code(d.getCode())
                .description(d.getDescription())
                .headId(d.getHead() != null ? d.getHead().getId() : null)
                .headName(d.getHead() != null ? d.getHead().getFullName() : null)
                .employeeCount(count)
                .active(d.getActive())
                .build();
    }
}
