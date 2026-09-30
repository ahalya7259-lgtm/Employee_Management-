package com.employeehub.department.repository;

import com.employeehub.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByCode(String code);
    boolean existsByName(String name);
    boolean existsByCode(String code);
    List<Department> findByActiveTrue();

    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.head WHERE d.id = :id")
    Optional<Department> findByIdWithHead(Long id);
}
