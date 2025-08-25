package com.biobac.warehouse.response;

import com.biobac.warehouse.dto.PaginationMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResponse<T> {
    private List<T> content;
    private PaginationMetadata metadata;
}