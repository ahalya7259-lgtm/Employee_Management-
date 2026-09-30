package com.employeehub.department.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Long headId;
    private String headName;
    private Long employeeCount;
    private Boolean active;
}
