package com.employeehub.attendance.repository;

import com.employeehub.attendance.entity.AttendanceRecord;
import com.employeehub.common.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);

    boolean existsByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);

    Page<AttendanceRecord> findByEmployeeIdOrderByAttendanceDateDesc(Long employeeId, Pageable pageable);

    @Query("""
        SELECT a FROM AttendanceRecord a
        JOIN FETCH a.employee e
        WHERE (:employeeId IS NULL OR a.employee.id = :employeeId)
        AND (:fromDate IS NULL OR a.attendanceDate >= :fromDate)
        AND (:toDate IS NULL OR a.attendanceDate <= :toDate)
        AND (:status IS NULL OR a.status = :status)
        ORDER BY a.attendanceDate DESC
        """)
    Page<AttendanceRecord> search(
            @Param("employeeId") Long employeeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") AttendanceStatus status,
            Pageable pageable);

    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.attendanceDate = :date AND a.status = :status")
    long countByDateAndStatus(@Param("date") LocalDate date, @Param("status") AttendanceStatus status);

    @Query("SELECT a FROM AttendanceRecord a WHERE a.employee.id = :employeeId AND a.attendanceDate BETWEEN :from AND :to")
    List<AttendanceRecord> findByEmployeeAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
