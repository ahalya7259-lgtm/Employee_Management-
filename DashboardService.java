package com.employeehub.dashboard.service;

import com.employeehub.attendance.repository.AttendanceRepository;
import com.employeehub.common.enums.AttendanceStatus;
import com.employeehub.common.enums.EmploymentStatus;
import com.employeehub.common.enums.LeaveStatus;
import com.employeehub.dashboard.dto.DashboardStats;
import com.employeehub.department.repository.DepartmentRepository;
import com.employeehub.employee.entity.Employee;
import com.employeehub.employee.repository.EmployeeRepository;
import com.employeehub.leave.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    @Transactional(readOnly = true)
    public DashboardStats getStats() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        long total = employeeRepository.countByDeletedAtIsNull();
        long active = employeeRepository.countByEmploymentStatusAndDeletedAtIsNull(EmploymentStatus.ACTIVE);
        long depts = departmentRepository.count();
        long present = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.PRESENT)
                + attendanceRepository.countByDateAndStatus(today, AttendanceStatus.LATE)
                + attendanceRepository.countByDateAndStatus(today, AttendanceStatus.HALF_DAY);
        long late = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.LATE);
        long pendingLeaves = leaveRequestRepository.countByStatus(LeaveStatus.PENDING);

        List<Map<String, Object>> distribution = departmentRepository.findAll().stream()
                .map(d -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", d.getName());
                    m.put("count", employeeRepository.countByDepartmentId(d.getId()));
                    return m;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> recent = employeeRepository.search(null, null, null, null, PageRequest.of(0, 5))
                .getContent().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", e.getId());
                    m.put("name", e.getFullName());
                    m.put("code", e.getEmployeeCode());
                    m.put("department", e.getDepartment() != null ? e.getDepartment().getName() : null);
                    m.put("joiningDate", e.getJoiningDate());
                    return m;
                })
                .collect(Collectors.toList());

        return DashboardStats.builder()
                .totalEmployees(total)
                .activeEmployees(active)
                .departmentCount(depts)
                .todayPresent(present)
                .todayAbsent(Math.max(0, active - present))
                .todayLate(late)
                .pendingLeaveRequests(pendingLeaves)
                .departmentDistribution(distribution)
                .recentEmployees(recent)
                .build();
    }
}
