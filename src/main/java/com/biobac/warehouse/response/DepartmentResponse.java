package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DepartmentResponse {
    private Long id;
    private String name;
    private String code;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
