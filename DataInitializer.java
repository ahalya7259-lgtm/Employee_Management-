package com.employeehub.config;

import com.employeehub.auth.entity.Role;
import com.employeehub.auth.entity.User;
import com.employeehub.auth.repository.RoleRepository;
import com.employeehub.auth.repository.UserRepository;
import com.employeehub.common.enums.EmploymentStatus;
import com.employeehub.common.enums.RoleName;
import com.employeehub.department.entity.Department;
import com.employeehub.department.repository.DepartmentRepository;
import com.employeehub.employee.entity.Employee;
import com.employeehub.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        ensureRoles();
        ensureDemoUsers();
        log.info("Data initialization complete");
    }

    private void ensureRoles() {
        for (RoleName name : RoleName.values()) {
            if (roleRepository.findByName(name).isEmpty()) {
                roleRepository.save(Role.builder()
                        .name(name)
                        .description(name.name() + " role")
                        .build());
            }
        }
    }

    private void ensureDemoUsers() {
        createUserIfMissing("admin@employeehub.com", "Admin@123", RoleName.ADMIN,
                "System", "Admin", "EMP-001", "HR", "System Administrator", new BigDecimal("120000"));
        createUserIfMissing("hr@employeehub.com", "Hr@12345", RoleName.HR,
                "Sarah", "Johnson", "EMP-002", "HR", "HR Manager", new BigDecimal("95000"));
        createUserIfMissing("john.doe@employeehub.com", "Emp@12345", RoleName.EMPLOYEE,
                "John", "Doe", "EMP-003", "ENG", "Senior Software Engineer", new BigDecimal("110000"));
        createUserIfMissing("jane.smith@employeehub.com", "Emp@12345", RoleName.EMPLOYEE,
                "Jane", "Smith", "EMP-004", "ENG", "Software Engineer", new BigDecimal("85000"));
    }

    private void createUserIfMissing(String email, String rawPassword, RoleName roleName,
                                     String firstName, String lastName, String empCode,
                                     String deptCode, String designation, BigDecimal salary) {
        if (userRepository.existsByEmail(email)) {
            // Ensure password is correct for demo
            userRepository.findByEmail(email).ifPresent(u -> {
                u.setPasswordHash(passwordEncoder.encode(rawPassword));
                userRepository.save(u);
            });
            return;
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName));

        User user = userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(role)
                .active(true)
                .build());

        if (!employeeRepository.existsByEmployeeCode(empCode)) {
            Department dept = departmentRepository.findByCode(deptCode).orElse(null);
            employeeRepository.save(Employee.builder()
                    .employeeCode(empCode)
                    .user(user)
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email)
                    .department(dept)
                    .designation(designation)
                    .employmentStatus(EmploymentStatus.ACTIVE)
                    .joiningDate(LocalDate.of(2023, 1, 15))
                    .salary(salary)
                    .build());
        }
    }
}
