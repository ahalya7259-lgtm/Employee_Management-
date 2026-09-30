package com.employeehub.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardStats {
    private long totalEmployees;
    private long activeEmployees;
    private long departmentCount;
    private long todayPresent;
    private long todayAbsent;
    private long todayLate;
    private long pendingLeaveRequests;
    private List<Map<String, Object>> departmentDistribution;
    private List<Map<String, Object>> recentEmployees;
}
