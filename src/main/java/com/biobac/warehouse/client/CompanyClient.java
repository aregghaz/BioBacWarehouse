package com.biobac.warehouse.client;

import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.CompanyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "company-service", url = "${services.company-url}")
public interface CompanyClient {
    @GetMapping("/{id}")
    ApiResponse<CompanyResponse> getCompany(@PathVariable Long id);

    @GetMapping("/name/{id}")
    ApiResponse<String> getCompanyName(@PathVariable Long id);
}
