package com.employeehub.leave.controller;

import com.employeehub.common.ApiResponse;
import com.employeehub.common.PageResponse;
import com.employeehub.common.enums.LeaveStatus;
import com.employeehub.employee.repository.EmployeeRepository;
import com.employeehub.leave.dto.LeaveRequestDto;
import com.employeehub.leave.dto.LeaveResponse;
import com.employeehub.leave.entity.LeaveBalance;
import com.employeehub.leave.service.LeaveService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
@Tag(name = "Leave Management")
@SecurityRequirement(name = "bearerAuth")
public class LeaveController {

    private final LeaveService leaveService;
    private final EmployeeRepository employeeRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<LeaveResponse>> submit(
            @Valid @RequestBody LeaveRequestDto dto,
            @RequestParam(required = false) Long employeeId,
            Authentication auth) {
        Long id = resolveEmployeeId(employeeId, auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Leave request submitted", leaveService.submit(id, dto)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<LeaveResponse>> approve(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication auth) {
        String comments = body != null ? body.get("comments") : null;
        return ResponseEntity.ok(ApiResponse.ok("Leave approved",
                leaveService.approve(id, comments, auth.getName())));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<LeaveResponse>> reject(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication auth) {
        String comments = body != null ? body.get("comments") : null;
        return ResponseEntity.ok(ApiResponse.ok("Leave rejected",
                leaveService.reject(id, comments, auth.getName())));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<PageResponse<LeaveResponse>>> list(
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        boolean isStaff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));
        Long empId = isStaff ? employeeId : resolveEmployeeId(null, auth);
        return ResponseEntity.ok(ApiResponse.ok(
                leaveService.search(status, empId, PageRequest.of(page, size))));
    }

    @GetMapping("/balances")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<LeaveBalance>>> balances(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer year,
            Authentication auth) {
        Long id = resolveEmployeeId(employeeId, auth);
        int y = year != null ? year : Year.now().getValue();
        return ResponseEntity.ok(ApiResponse.ok(leaveService.getBalances(id, y)));
    }

    private Long resolveEmployeeId(Long employeeId, Authentication auth) {
        if (employeeId != null) {
            boolean isStaff = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"));
            if (isStaff) return employeeId;
        }
        return employeeRepository.findAll().stream()
                .filter(e -> e.getUser() != null && e.getUser().getEmail().equals(auth.getName()))
                .findFirst()
                .map(e -> e.getId())
                .orElseThrow(() -> new RuntimeException("Employee profile not linked to user"));
    }
}
