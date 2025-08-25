package com.biobac.warehouse.controller;

import com.biobac.warehouse.client.CompanyClient;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.ProductDto;
import com.biobac.warehouse.entity.Entities;
import com.biobac.warehouse.exception.ExternalServiceException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.ProductMapper;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.ProductCreateRequest;
import com.biobac.warehouse.request.ProductUpdateRequest;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.CompanyResponse;
import com.biobac.warehouse.response.ProductResponse;
import com.biobac.warehouse.response.ProductTableResponse;
import com.biobac.warehouse.service.AuditLogService;
import com.biobac.warehouse.service.ProductService;
import com.biobac.warehouse.utils.ResponseUtil;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController extends BaseController {
    private final ProductService productService;
    private final AuditLogService auditLogService;
    private final CompanyClient companyClient;
    private final ProductMapper productMapper;

    @GetMapping
    public ApiResponse<List<ProductResponse>> getAll() {
        List<ProductResponse> products = productService.getAll().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
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
    public ApiResponse<ProductResponse> getById(@PathVariable Long id) {
        ProductDto product = productService.getById(id);
        if (product.getCompanyId() != null) {
            try {
                ApiResponse<CompanyResponse> x = companyClient.getCompany(product.getCompanyId());
                if (!x.getSuccess()) {
                    throw new NotFoundException("Company not found with id " + product.getCompanyId());
                }
            } catch (FeignException ex) {
                throw new ExternalServiceException("External service error, please try later");
            }
        }
        return ResponseUtil.success("Product retrieved successfully", productMapper.toResponse(product));
    }

    @PostMapping
    public ApiResponse<ProductResponse> create(@RequestBody ProductCreateRequest dto, HttpServletRequest request) {
//        try {
//            ApiResponse<CompanyResponse> x = companyClient.getCompany(dto.getCompanyId());
//            if (!x.getSuccess()) {
//                throw new NotFoundException("Company not found with id" + " " + dto.getCompanyId());
//            }
//        } catch (FeignException ex) {
//            throw new ExternalServiceException("External service error, please try later");
//        }
        ProductDto productDto = productService.create(dto);
        auditLogService.logCreate(Entities.PRODUCT.name(), productDto.getId(), dto, getUsername(request));
        return ResponseUtil.success("Product created successfully", productMapper.toResponse(productDto));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(@PathVariable Long id, @RequestBody ProductUpdateRequest dto, HttpServletRequest request) {
//        if (dto.getCompanyId() != null) {
//            try {
//                ApiResponse<CompanyResponse> x = companyClient.getCompany(dto.getCompanyId());
//                if (!x.getSuccess()) {
//                    throw new NotFoundException("Company not found with id " + dto.getCompanyId());
//                }
//            } catch (FeignException ex) {
//                throw new ExternalServiceException("External service error, please try later");
//            }
//        }
        ProductDto existingProduct = productService.getById(id);
        ProductDto productDto = productService.update(id, dto);
        auditLogService.logUpdate(Entities.PRODUCT.name(), productDto.getId(), existingProduct, dto, getUsername(request));
        return ResponseUtil.success("Product updated successfully", productMapper.toResponse(productDto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id, HttpServletRequest request) {
        productService.delete(id);
        auditLogService.logDelete(Entities.PRODUCT.name(), id, getUsername(request));
        return ResponseUtil.success("Product deleted successfully");
    }
}
