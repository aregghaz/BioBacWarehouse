package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.ReceiveIngredientUpdateWrapper;
import com.biobac.warehouse.request.ReceiveIngredientWrapper;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.ReceiveIngredientResponse;
import com.biobac.warehouse.response.ReceiveIngredientGroupResponse;
import com.biobac.warehouse.service.ReceiveIngredientService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/receive-ingredient")
@RequiredArgsConstructor
public class ReceiveIngredientController {
    private final ReceiveIngredientService receiveIngredientService;

    @PostMapping
    public ApiResponse<List<ReceiveIngredientResponse>> receive(
            @RequestBody ReceiveIngredientWrapper wrapper) {

        List<ReceiveIngredientResponse> response = receiveIngredientService.receive(
                wrapper.getRequests(),
                wrapper.getExpenseRequests()
        );

        return ResponseUtil.success("Ingredients received successfully", response);
    }

    @PostMapping("/all")
    public ApiResponse<List<ReceiveIngredientResponse>> getByIngredientId(@RequestParam Long ingredientId,
                                                                          @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                          @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                          @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                                          @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                                          @RequestBody Map<String, FilterCriteria> filters) {
        Pair<List<ReceiveIngredientResponse>, PaginationMetadata> result = receiveIngredientService.getByIngredientId(ingredientId, filters, page, size, sortBy, sortDir);
        return ResponseUtil.success("Received ingredients retrieved successfully", result.getFirst(), result.getSecond());
    }

    @GetMapping("/{groupId}")
    public ApiResponse<ReceiveIngredientGroupResponse> getByGroupId(@PathVariable Long groupId) {
        ReceiveIngredientGroupResponse response = receiveIngredientService.getByGroupId(groupId);
        return ResponseUtil.success("Receive group retrieved successfully", response);
    }

    @PutMapping("/{groupId}")
    public ApiResponse<List<ReceiveIngredientResponse>> update(@PathVariable Long groupId, @RequestBody ReceiveIngredientUpdateWrapper wrapper) {
        List<ReceiveIngredientResponse> response = receiveIngredientService.update(
                groupId,
                wrapper.getRequests(),
                wrapper.getExpenseRequests()
        );
        return ResponseUtil.success("Ingredients receive updated successfully", response);
    }

    @DeleteMapping("/{groupId}")
    public ApiResponse<String> delete(@PathVariable Long groupId) {
        receiveIngredientService.delete(groupId);
        return ResponseUtil.success("Ingredients receive deleted successfully");
    }
}
