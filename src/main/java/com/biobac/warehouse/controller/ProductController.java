package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.ProductCreateRequest;
import com.biobac.warehouse.request.ProductUpdateRequest;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.ProductResponse;
import com.biobac.warehouse.service.ProductService;
import com.biobac.warehouse.utils.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request){
        ProductResponse response = productService.create(request);
        return ResponseUtil.success("Product created successfully", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getById(@PathVariable Long id) {
        ProductResponse response = productService.getById(id);
        return ResponseUtil.success("Product retrieved successfully", response);
    }

    @GetMapping
    public ApiResponse<List<ProductResponse>> getAll() {
        List<ProductResponse> responses = productService.getAll();
        return ResponseUtil.success("Products retrieved successfully", responses);
    }

    @PostMapping("/all")
    public ApiResponse<List<ProductResponse>> getAllProducts(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                             @RequestParam(required = false, defaultValue = "10") Integer size,
                                                             @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                             @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                             @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<ProductResponse>, PaginationMetadata> result = productService.getPagination(filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Products retrieved successfully", result.getFirst(), result.getSecond());
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(@PathVariable Long id, @RequestBody ProductUpdateRequest request) {
        ProductResponse response = productService.update(id, request);
        return ResponseUtil.success("Product updated successfully", response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseUtil.success("Product deleted successfully");
    }
}
