package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.ProductDto;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.service.ProductService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService service;

    @GetMapping
    public ApiResponse<List<ProductDto>> getAll() {
        List<ProductDto> products = service.getAll();
        return ResponseUtil.success("Products retrieved successfully", products);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDto> getById(@PathVariable Long id) {
        ProductDto product = service.getById(id);
        return ResponseUtil.success("Product retrieved successfully", product);
    }

    @PostMapping
    public ApiResponse<ProductDto> create(@RequestBody ProductDto dto) {
        dto.setId(null); // Ensure it's treated as new
        ProductDto productDto = service.create(dto);
        return ResponseUtil.success("Product created successfully", productDto);
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductDto> update(@PathVariable Long id, @RequestBody ProductDto dto) {
        ProductDto productDto = service.update(id, dto);
        return ResponseUtil.success("Product updated successfully", productDto);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseUtil.success("Product deleted successfully");
    }
}
