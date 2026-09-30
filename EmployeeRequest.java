package com.employeehub.employee.dto;

import com.employeehub.common.enums.EmploymentStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @Size(max = 30)
    private String phone;

    private LocalDate dateOfBirth;

    @Size(max = 20)
    private String gender;

    @Size(max = 500)
    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 100)
    private String country;

    @Size(max = 20)
    private String postalCode;

    private Long departmentId;

    @Size(max = 150)
    private String designation;

    private EmploymentStatus employmentStatus;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be positive")
    private BigDecimal salary;

    @Size(max = 150)
    private String emergencyContactName;

    @Size(max = 30)
    private String emergencyContactPhone;

    @Size(max = 500)
    private String profileImageUrl;

    private Boolean createUserAccount;
    private String role; // ADMIN, HR, EMPLOYEE when creating account
    private String password;
}
