package com.employeehub.employee.repository;

import com.employeehub.common.enums.EmploymentStatus;
import com.employeehub.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCode(String employeeCode);
    Optional<Employee> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmployeeCode(String employeeCode);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department LEFT JOIN FETCH e.user WHERE e.id = :id AND e.deletedAt IS NULL")
    Optional<Employee> findByIdActive(@Param("id") Long id);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department WHERE e.user.id = :userId AND e.deletedAt IS NULL")
    Optional<Employee> findByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT e FROM Employee e
        LEFT JOIN e.department d
        WHERE e.deletedAt IS NULL
        AND (:search IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:departmentId IS NULL OR e.department.id = :departmentId)
        AND (:status IS NULL OR e.employmentStatus = :status)
        AND (:designation IS NULL OR LOWER(e.designation) LIKE LOWER(CONCAT('%', :designation, '%')))
        """)
    Page<Employee> search(
            @Param("search") String search,
            @Param("departmentId") Long departmentId,
            @Param("status") EmploymentStatus status,
            @Param("designation") String designation,
            Pageable pageable);

    long countByEmploymentStatusAndDeletedAtIsNull(EmploymentStatus status);
    long countByDeletedAtIsNull();

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId AND e.deletedAt IS NULL")
    long countByDepartmentId(@Param("departmentId") Long departmentId);
}
