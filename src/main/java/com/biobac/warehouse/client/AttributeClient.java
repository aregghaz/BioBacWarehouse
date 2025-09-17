package com.biobac.warehouse.client;

import com.biobac.warehouse.request.AttributeUpsertRequest;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.AttributeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "attribute-service", url = "${services.attribute-url}")
public interface AttributeClient {
    @PostMapping("/attribute-value")
    ApiResponse<List<AttributeResponse>> createValues(
            @RequestParam("targetId") Long targetId,
            @RequestParam("targetType") String targetType,
            @RequestBody List<AttributeUpsertRequest> attributes
    );

    @GetMapping("/attribute-value/{targetId}/{targetType}")
    ApiResponse<List<AttributeResponse>> getValues(
            @PathVariable("targetId") Long targetId,
            @PathVariable("targetType") String targetType
    );

    @DeleteMapping("/attribute-value/{targetId}/{targetType}")
    ApiResponse<List<String>> deleteValues(
            @PathVariable("targetId") Long targetId,
            @PathVariable("targetType") String targetType
    );
}
