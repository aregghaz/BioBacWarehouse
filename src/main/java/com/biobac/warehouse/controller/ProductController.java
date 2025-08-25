package com.biobac.warehouse.controller;

import com.biobac.warehouse.request.ProductCreateRequest;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.ProductResponse;
import com.biobac.warehouse.service.ProductService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ApiResponse<ProductResponse> create(@RequestBody ProductCreateRequest request){
        ProductResponse response = productService.create(request);

        return ResponseUtil.success("Product created successfully", response);
    }
}
