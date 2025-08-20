package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.ProductDto;
import com.biobac.warehouse.entity.Entities;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.ProductTableResponse;
import com.biobac.warehouse.service.AuditLogService;
import com.biobac.warehouse.service.ProductService;
import com.biobac.warehouse.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController extends BaseController {
    private final ProductService productService;
    private final AuditLogService auditLogService;

    @GetMapping
    public ApiResponse<List<ProductDto>> getAll() {
        List<ProductDto> products = productService.getAll();
        return ResponseUtil.success("Products retrieved successfully", products);
    }

    @PostMapping("/all")
    public ApiResponse<List<ProductTableResponse>> getAll(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                          @RequestParam(required = false, defaultValue = "10") Integer size,
                                                          @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                          @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                          @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<ProductTableResponse>, PaginationMetadata> result = productService.getPagination(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Products retrieved successfully", result.getFirst(), result.getSecond());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDto> getById(@PathVariable Long id) {
        ProductDto product = productService.getById(id);
        return ResponseUtil.success("Product retrieved successfully", product);
    }

    @PostMapping
    public ApiResponse<ProductDto> create(@RequestBody ProductDto dto, HttpServletRequest request) {
        dto.setId(null);
        ProductDto productDto = productService.create(dto);
        auditLogService.logCreate(Entities.PRODUCT.name(), productDto.getId(), dto, getUsername(request));
        return ResponseUtil.success("Product created successfully", productDto);
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductDto> update(@PathVariable Long id, @RequestBody ProductDto dto, HttpServletRequest request) {
        ProductDto existingProduct = productService.getById(id);
        ProductDto productDto = productService.update(id, dto);
        auditLogService.logUpdate(Entities.PRODUCT.name(), productDto.getId(), existingProduct, dto, getUsername(request));
        return ResponseUtil.success("Product updated successfully", productDto);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseUtil.success("Product deleted successfully");
    }
}
