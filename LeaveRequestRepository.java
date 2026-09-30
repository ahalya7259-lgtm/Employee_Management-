package com.employeehub.leave.repository;

import com.employeehub.common.enums.LeaveStatus;
import com.employeehub.leave.entity.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    Page<LeaveRequest> findByEmployeeIdOrderByAppliedAtDesc(Long employeeId, Pageable pageable);

    Page<LeaveRequest> findByStatusOrderByAppliedAtDesc(LeaveStatus status, Pageable pageable);

    long countByStatus(LeaveStatus status);

    @Query("""
        SELECT lr FROM LeaveRequest lr
        WHERE lr.employee.id = :employeeId
        AND lr.status IN ('PENDING', 'APPROVED')
        AND lr.startDate <= :endDate
        AND lr.endDate >= :startDate
        AND (:excludeId IS NULL OR lr.id <> :excludeId)
        """)
    List<LeaveRequest> findOverlapping(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") Long excludeId);

    @Query("""
        SELECT lr FROM LeaveRequest lr
        JOIN FETCH lr.employee
        WHERE (:status IS NULL OR lr.status = :status)
        AND (:employeeId IS NULL OR lr.employee.id = :employeeId)
        ORDER BY lr.appliedAt DESC
        """)
    Page<LeaveRequest> search(
            @Param("status") LeaveStatus status,
            @Param("employeeId") Long employeeId,
            Pageable pageable);
}
