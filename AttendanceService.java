package com.employeehub.attendance.service;

import com.employeehub.attendance.dto.AttendanceResponse;
import com.employeehub.attendance.entity.AttendanceRecord;
import com.employeehub.attendance.repository.AttendanceRepository;
import com.employeehub.common.PageResponse;
import com.employeehub.common.enums.AttendanceStatus;
import com.employeehub.employee.entity.Employee;
import com.employeehub.employee.repository.EmployeeRepository;
import com.employeehub.exception.BadRequestException;
import com.employeehub.exception.ConflictException;
import com.employeehub.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    // Timezone strategy: All attendance timestamps stored in UTC.
    // Attendance date is the UTC calendar date of check-in.

    @Transactional
    public AttendanceResponse checkIn(Long employeeId) {
        Employee employee = employeeRepository.findByIdActive(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        if (attendanceRepository.existsByEmployeeIdAndAttendanceDate(employeeId, today)) {
            throw new ConflictException("Already checked in today");
        }

        Instant now = Instant.now();
        AttendanceStatus status = AttendanceStatus.PRESENT;
        // Simple late rule: after 09:30 UTC considered late (adjust per org policy)
        if (now.atZone(ZoneOffset.UTC).getHour() > 9
                || (now.atZone(ZoneOffset.UTC).getHour() == 9 && now.atZone(ZoneOffset.UTC).getMinute() > 30)) {
            status = AttendanceStatus.LATE;
        }

        AttendanceRecord record = AttendanceRecord.builder()
                .employee(employee)
                .attendanceDate(today)
                .checkInAt(now)
                .status(status)
                .build();
        return toResponse(attendanceRepository.save(record));
    }

    @Transactional
    public AttendanceResponse checkOut(Long employeeId) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        AttendanceRecord record = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, today)
                .orElseThrow(() -> new BadRequestException("No check-in found for today"));

        if (record.getCheckOutAt() != null) {
            throw new ConflictException("Already checked out today");
        }

        Instant now = Instant.now();
        record.setCheckOutAt(now);
        if (record.getCheckInAt() != null) {
            long minutes = Duration.between(record.getCheckInAt(), now).toMinutes();
            BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            record.setWorkHours(hours);
            if (hours.compareTo(BigDecimal.valueOf(4)) < 0) {
                record.setStatus(AttendanceStatus.HALF_DAY);
            }
        }
        return toResponse(attendanceRepository.save(record));
    }

    @Transactional(readOnly = true)
    public PageResponse<AttendanceResponse> search(Long employeeId, LocalDate from, LocalDate to,
                                                    AttendanceStatus status, Pageable pageable) {
        Page<AttendanceRecord> page = attendanceRepository.search(employeeId, from, to, status, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public AttendanceResponse getToday(Long employeeId) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, today)
                .map(this::toResponse)
                .orElse(null);
    }

    private AttendanceResponse toResponse(AttendanceRecord a) {
        return AttendanceResponse.builder()
                .id(a.getId())
                .employeeId(a.getEmployee().getId())
                .employeeName(a.getEmployee().getFullName())
                .employeeCode(a.getEmployee().getEmployeeCode())
                .attendanceDate(a.getAttendanceDate())
                .checkInAt(a.getCheckInAt())
                .checkOutAt(a.getCheckOutAt())
                .status(a.getStatus())
                .workHours(a.getWorkHours())
                .notes(a.getNotes())
                .build();
    }
}
