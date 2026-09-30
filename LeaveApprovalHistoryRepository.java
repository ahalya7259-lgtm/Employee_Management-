package com.employeehub.leave.repository;

import com.employeehub.leave.entity.LeaveApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveApprovalHistoryRepository extends JpaRepository<LeaveApprovalHistory, Long> {
    List<LeaveApprovalHistory> findByLeaveRequestIdOrderByPerformedAtAsc(Long leaveRequestId);
}
