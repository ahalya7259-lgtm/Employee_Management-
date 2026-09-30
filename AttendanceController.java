package com.employeehub.attendance.controller;

import com.employeehub.attendance.dto.AttendanceResponse;
import com.employeehub.attendance.service.AttendanceService;
import com.employeehub.common.ApiResponse;
import com.employeehub.common.PageResponse;
import com.employeehub.common.enums.AttendanceStatus;
import com.employeehub.employee.repository.EmployeeRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EmployeeRepository employeeRepository;

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(
            @RequestParam(required = false) Long employeeId, Authentication auth) {
        Long id = resolveEmployeeId(employeeId, auth);
        return ResponseEntity.ok(ApiResponse.ok("Checked in", attendanceService.checkIn(id)));
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkOut(
            @RequestParam(required = false) Long employeeId, Authentication auth) {
        Long id = resolveEmployeeId(employeeId, auth);
        return ResponseEntity.ok(ApiResponse.ok("Checked out", attendanceService.checkOut(id)));
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> today(
            @RequestParam(required = false) Long employeeId, Authentication auth) {
        Long id = resolveEmployeeId(employeeId, auth);
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getToday(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<PageResponse<AttendanceResponse>>> search(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) AttendanceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                attendanceService.search(employeeId, from, to, status, PageRequest.of(page, size))));
    }

    private Long resolveEmployeeId(Long employeeId, Authentication auth) {
        if (employeeId != null && (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR")))) {
            return employeeId;
        }
        return employeeRepository.findByUserId(
                // resolve from email - simplified: look up by email from principal
                employeeRepository.findAll().stream()
                        .filter(e -> e.getUser() != null && e.getUser().getEmail().equals(auth.getName()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Employee profile not linked"))
                        .getUser().getId()
        ).orElseThrow(() -> new RuntimeException("Employee profile not linked")).getId();
    }
}
