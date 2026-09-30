package com.employeehub.employee.service;

import com.employeehub.auth.entity.Role;
import com.employeehub.auth.entity.User;
import com.employeehub.auth.repository.RoleRepository;
import com.employeehub.auth.repository.UserRepository;
import com.employeehub.common.PageResponse;
import com.employeehub.common.enums.EmploymentStatus;
import com.employeehub.common.enums.RoleName;
import com.employeehub.department.entity.Department;
import com.employeehub.department.repository.DepartmentRepository;
import com.employeehub.employee.dto.EmployeeRequest;
import com.employeehub.employee.dto.EmployeeResponse;
import com.employeehub.employee.entity.Employee;
import com.employeehub.employee.repository.EmployeeRepository;
import com.employeehub.exception.BadRequestException;
import com.employeehub.exception.ConflictException;
import com.employeehub.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final AtomicLong CODE_SEQ = new AtomicLong(100);

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> search(String search, Long departmentId,
                                                  EmploymentStatus status, String designation,
                                                  Pageable pageable, boolean includeSalary) {
        Page<Employee> page = employeeRepository.search(search, departmentId, status, designation, pageable);
        return PageResponse.from(page.map(e -> toResponse(e, includeSalary)));
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id, boolean includeSalary) {
        Employee employee = employeeRepository.findByIdActive(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        return toResponse(employee, includeSalary);
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request, boolean includeSalary) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        }

        String code = generateEmployeeCode();
        Employee employee = Employee.builder()
                .employeeCode(code)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .department(department)
                .designation(request.getDesignation())
                .employmentStatus(request.getEmploymentStatus() != null
                        ? request.getEmploymentStatus() : EmploymentStatus.ACTIVE)
                .joiningDate(request.getJoiningDate())
                .salary(request.getSalary())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .profileImageUrl(request.getProfileImageUrl())
                .build();

        if (Boolean.TRUE.equals(request.getCreateUserAccount())) {
            if (request.getPassword() == null || request.getPassword().length() < 6) {
                throw new BadRequestException("Password must be at least 6 characters when creating user account");
            }
            RoleName roleName = request.getRole() != null
                    ? RoleName.valueOf(request.getRole()) : RoleName.EMPLOYEE;
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new BadRequestException("Invalid role"));
            User user = userRepository.save(User.builder()
                    .email(request.getEmail())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .role(role)
                    .active(true)
                    .build());
            employee.setUser(user);
        }

        employee = employeeRepository.save(employee);
        return toResponse(employee, includeSalary);
    }

    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request, boolean includeSalary) {
        Employee employee = employeeRepository.findByIdActive(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));

        if (!employee.getEmail().equals(request.getEmail())
                && employeeRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            employee.setDepartment(department);
        } else {
            employee.setDepartment(null);
        }

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setGender(request.getGender());
        employee.setAddress(request.getAddress());
        employee.setCity(request.getCity());
        employee.setState(request.getState());
        employee.setCountry(request.getCountry());
        employee.setPostalCode(request.getPostalCode());
        employee.setDesignation(request.getDesignation());
        if (request.getEmploymentStatus() != null) {
            employee.setEmploymentStatus(request.getEmploymentStatus());
        }
        employee.setJoiningDate(request.getJoiningDate());
        if (request.getSalary() != null) {
            employee.setSalary(request.getSalary());
        }
        employee.setEmergencyContactName(request.getEmergencyContactName());
        employee.setEmergencyContactPhone(request.getEmergencyContactPhone());
        employee.setProfileImageUrl(request.getProfileImageUrl());

        employee = employeeRepository.save(employee);
        return toResponse(employee, includeSalary);
    }

    @Transactional
    public void softDelete(Long id) {
        Employee employee = employeeRepository.findByIdActive(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        employee.setDeletedAt(Instant.now());
        employee.setEmploymentStatus(EmploymentStatus.TERMINATED);
        if (employee.getUser() != null) {
            employee.getUser().setActive(false);
        }
        employeeRepository.save(employee);
    }

    private String generateEmployeeCode() {
        long seq = CODE_SEQ.incrementAndGet();
        String code;
        do {
            code = String.format("EMP-%03d", seq++);
        } while (employeeRepository.existsByEmployeeCode(code));
        return code;
    }

    private EmployeeResponse toResponse(Employee e, boolean includeSalary) {
        return EmployeeResponse.builder()
                .id(e.getId())
                .employeeCode(e.getEmployeeCode())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .fullName(e.getFullName())
                .email(e.getEmail())
                .phone(e.getPhone())
                .dateOfBirth(e.getDateOfBirth())
                .gender(e.getGender())
                .address(e.getAddress())
                .city(e.getCity())
                .state(e.getState())
                .country(e.getCountry())
                .postalCode(e.getPostalCode())
                .departmentId(e.getDepartment() != null ? e.getDepartment().getId() : null)
                .departmentName(e.getDepartment() != null ? e.getDepartment().getName() : null)
                .designation(e.getDesignation())
                .employmentStatus(e.getEmploymentStatus())
                .joiningDate(e.getJoiningDate())
                .resignationDate(e.getResignationDate())
                .salary(includeSalary ? e.getSalary() : null)
                .emergencyContactName(e.getEmergencyContactName())
                .emergencyContactPhone(e.getEmergencyContactPhone())
                .profileImageUrl(e.getProfileImageUrl())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
