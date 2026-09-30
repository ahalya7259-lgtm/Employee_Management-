package com.employeehub.attendance.dto;

import com.employeehub.common.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class AttendanceResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private LocalDate attendanceDate;
    private Instant checkInAt;
    private Instant checkOutAt;
    private AttendanceStatus status;
    private BigDecimal workHours;
    private String notes;
}
