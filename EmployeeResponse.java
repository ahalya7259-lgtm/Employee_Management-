package com.employeehub.employee.dto;

import com.employeehub.common.enums.EmploymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class EmployeeResponse {
    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private Long departmentId;
    private String departmentName;
    private String designation;
    private EmploymentStatus employmentStatus;
    private LocalDate joiningDate;
    private LocalDate resignationDate;
    private BigDecimal salary; // only for ADMIN/HR
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String profileImageUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
