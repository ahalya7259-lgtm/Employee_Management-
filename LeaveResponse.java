package com.employeehub.leave.dto;

import com.employeehub.common.enums.LeaveStatus;
import com.employeehub.common.enums.LeaveType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class LeaveResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalDays;
    private String reason;
    private LeaveStatus status;
    private Instant appliedAt;
    private String reviewedBy;
    private Instant reviewedAt;
    private String reviewComments;
}
