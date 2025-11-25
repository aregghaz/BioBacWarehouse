package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.client.UserClient;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.ProductHistoryDto;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.entity.ProductHistory;
import com.biobac.warehouse.mapper.ProductHistoryMapper;
import com.biobac.warehouse.repository.HistoryActionRepository;
import com.biobac.warehouse.repository.ProductBalanceRepository;
import com.biobac.warehouse.repository.ProductHistoryRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.ProductHistoryResponse;
import com.biobac.warehouse.response.ProductHistorySingleResponse;
import com.biobac.warehouse.response.UserResponse;
import com.biobac.warehouse.service.ProductHistoryService;
import com.biobac.warehouse.utils.GroupUtil;
import com.biobac.warehouse.utils.specifications.ProductHistorySpecification;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.biobac.warehouse.utils.DateUtil.parseDates;

@Service
@RequiredArgsConstructor
public class ProductHistoryServiceImpl implements ProductHistoryService {

    private final UserClient userClient;

    private final ProductHistoryRepository productHistoryRepository;
    private final ProductHistoryMapper productHistoryMapper;
    private final GroupUtil groupUtil;
    private final HistoryActionRepository historyActionRepository;
    private final ProductBalanceRepository productBalanceRepository;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "id";
    private static final String DEFAULT_SORT_DIR = "desc";

    private Pageable buildPageable(Integer page, Integer size, String sortBy, String sortDir) {
        int safePage = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int safeSize = (size == null || size <= 0) ? DEFAULT_SIZE : size;
        if (safeSize > 1000) safeSize = 1000;

        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? DEFAULT_SORT_BY : sortBy.trim();
        String safeSortDir = (sortDir == null || sortDir.isBlank()) ? DEFAULT_SORT_DIR : sortDir.trim();

        Sort sort = safeSortDir.equalsIgnoreCase("asc")
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();

        return PageRequest.of(safePage, safeSize, sort);
    }

    @Override
    @Transactional
    public ProductHistorySingleResponse recordQuantityChange(ProductHistoryDto dto) {
        if (dto == null || dto.getProduct() == null) {
            throw new IllegalArgumentException("Product and dto are required");
        }
        Product product = dto.getProduct();
        double quantityResult = productHistoryRepository
                .findFirstByWarehouseAndProductOrderByTimestampDescIdDesc(dto.getWarehouse(), dto.getProduct())
                .map(ProductHistory::getQuantityResult)
                .orElse(0.0);

        ProductHistory history = new ProductHistory();
        history.setProduct(product);
        history.setWarehouse(dto.getWarehouse() != null ? dto.getWarehouse() : product.getDefaultWarehouse());

        Double change = Optional.ofNullable(dto.getQuantityChange()).orElse(0.0);
        history.setIncrease(change > 0);
        history.setQuantityChange(change);
        history.setQuantityResult(quantityResult + change);
        history.setNotes(dto.getNotes());
        history.setCompanyId(dto.getCompanyId());
        history.setLastPrice(dto.getLastPrice());
        history.setTimestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now());

        if (dto.getAction() != null) {
            history.setAction(dto.getAction());
        }

        ProductHistory saved = productHistoryRepository.save(history);
        return productHistoryMapper.toSingleResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ProductHistorySingleResponse>, PaginationMetadata> getHistoryForProduct(Long productId, Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "timestamp" : sortBy;
        String safeSortDir = (sortDir == null || sortDir.isBlank()) ? "desc" : sortDir;

        Sort sort = safeSortDir.equalsIgnoreCase("asc")
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();

        sort = sort.and(Sort.by("id").descending());

        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 20,
                sort
        );

        Specification<ProductHistory> spec = ProductHistorySpecification.buildSpecification(filters)
                .and((root, query, cb) -> root.join("product", JoinType.LEFT).get("id").in(productId));
        Page<ProductHistory> pageResult = productHistoryRepository.findAll(spec, pageable);

        List<ProductHistorySingleResponse> content = pageResult.getContent()
                .stream()
                .map(entity -> {
                    ProductHistorySingleResponse resp = productHistoryMapper.toSingleResponse(entity);
                    try {
                        if (entity.getUserId() != null) {
                            ApiResponse<UserResponse> ur = userClient.getUser(entity.getUserId());
                            if (ur != null && Boolean.TRUE.equals(ur.getSuccess()) && ur.getData() != null) {
                                String fn = ur.getData().getFirstname();
                                String ln = ur.getData().getLastname();
                                String un = ur.getData().getUsername();
                                StringBuilder sb = new StringBuilder();
                                if (fn != null && !fn.isBlank()) sb.append(fn);
                                if (ln != null && !ln.isBlank()) {
                                    if (!sb.isEmpty()) sb.append(' ');
                                    sb.append(ln);
                                }
                                if (un != null && !un.isBlank()) {
                                    if (!sb.isEmpty()) sb.append(' ');
                                    sb.append('(').append(un).append(')');
                                }
                                resp.setUsername(sb.toString());
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    return resp;
                })
                .collect(Collectors.toList());

        String metaSortDir = pageable.getSort().toString().contains("ASC") ? "asc" : "desc";
        String metaSortBy = pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse("id");

        PaginationMetadata metadata = new PaginationMetadata(
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast(),
                filters,
                metaSortDir,
                metaSortBy,
                "productHistoryTable"
        );
        return Pair.of(content, metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ProductHistorySingleResponse>, PaginationMetadata> getHistory(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        List<Long> productGroupIds = groupUtil.getAccessibleProductGroupIds();
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<ProductHistory> spec = ProductHistorySpecification.buildSpecification(filters)
                .and(ProductHistorySpecification.belongsToProductGroups(productGroupIds));
        Page<ProductHistory> pageResult = productHistoryRepository.findAll(spec, pageable);

        List<ProductHistorySingleResponse> content = pageResult.getContent()
                .stream()
                .map(entity -> {
                    ProductHistorySingleResponse resp = productHistoryMapper.toSingleResponse(entity);
                    try {
                        if (entity.getUserId() != null) {
                            ApiResponse<UserResponse> ur = userClient.getUser(entity.getUserId());
                            if (ur != null && Boolean.TRUE.equals(ur.getSuccess()) && ur.getData() != null) {
                                String fn = ur.getData().getFirstname();
                                String ln = ur.getData().getLastname();
                                String un = ur.getData().getUsername();
                                StringBuilder sb = new StringBuilder();
                                if (fn != null && !fn.isBlank()) sb.append(fn);
                                if (ln != null && !ln.isBlank()) {
                                    if (!sb.isEmpty()) sb.append(' ');
                                    sb.append(ln);
                                }
                                if (un != null && !un.isBlank()) {
                                    if (!sb.isEmpty()) sb.append(' ');
                                    sb.append('(').append(un).append(')');
                                }
                                resp.setUsername(sb.toString());
                            }
                        }
                    } catch (Exception ignored) {
                        // ignore user service failures
                    }
                    return resp;
                })
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "productsHistoryTable"
        );
        return Pair.of(content, metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ProductHistoryResponse>, PaginationMetadata> getAll(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        int safePage = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int safeSize = (size == null || size <= 0) ? DEFAULT_SIZE : size;
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "productName" : sortBy.trim();
        String safeSortDir = (sortDir == null || sortDir.isBlank()) ? DEFAULT_SORT_DIR : sortDir.trim();

        List<Long> productGroupIds = groupUtil.getAccessibleProductGroupIds();
        Specification<ProductHistory> spec = ProductHistorySpecification.buildSpecification(filters)
                .and(ProductHistorySpecification.belongsToProductGroups(productGroupIds));

        List<ProductHistory> matching = productHistoryRepository.findAll(spec);
        Map<Long, List<ProductHistory>> historiesByProduct = matching.stream()
                .collect(Collectors.groupingBy(h -> h.getProduct().getId()));

        List<LocalDateTime> dates = parseDates(filters);
        LocalDateTime startDate = dates.get(0);
        LocalDateTime endDate = dates.get(1);

        Map<Long, ProductHistory> representative = historiesByProduct.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .sorted((a, b) -> {
                                    int cmp = b.getTimestamp().compareTo(a.getTimestamp());
                                    if (cmp == 0) cmp = Long.compare(b.getId(), a.getId());
                                    return cmp;
                                })
                                .findFirst().orElse(null)
                ));

        List<Long> sortedProductIds = representative.keySet().stream().sorted((id1, id2) -> {
            ProductHistory h1 = representative.get(id1);
            ProductHistory h2 = representative.get(id2);
            int cmp;
            switch (safeSortBy) {
                case "productName":
                    String n1 = h1 != null && h1.getProduct() != null && h1.getProduct().getName() != null ? h1.getProduct().getName() : "";
                    String n2 = h2 != null && h2.getProduct() != null && h2.getProduct().getName() != null ? h2.getProduct().getName() : "";
                    cmp = n1.compareToIgnoreCase(n2);
                    break;
                case "unitName":
                    String u1 = h1 != null && h1.getProduct() != null && h1.getProduct().getUnit() != null && h1.getProduct().getUnit().getName() != null ? h1.getProduct().getUnit().getName() : "";
                    String u2 = h2 != null && h2.getProduct() != null && h2.getProduct().getUnit() != null && h2.getProduct().getUnit().getName() != null ? h2.getProduct().getUnit().getName() : "";
                    cmp = u1.compareToIgnoreCase(u2);
                    break;
                case "timestamp":
                    if (h1 == null && h2 == null) cmp = 0;
                    else if (h1 == null) cmp = -1;
                    else if (h2 == null) cmp = 1;
                    else {
                        cmp = h1.getTimestamp().compareTo(h2.getTimestamp());
                        if (cmp == 0) cmp = Long.compare(h1.getId(), h2.getId());
                    }
                    break;
                default:
                    cmp = Long.compare(id1, id2);
            }
            return safeSortDir.equalsIgnoreCase("asc") ? cmp : -cmp;
        }).toList();

        int totalProducts = sortedProductIds.size();
        int fromIndex = Math.min(safePage * safeSize, totalProducts);
        int toIndex = Math.min(fromIndex + safeSize, totalProducts);
        List<Long> pageOfIds = sortedProductIds.subList(fromIndex, toIndex);

        List<ProductHistoryResponse> responses = pageOfIds.stream().map(id -> {
            ProductHistory representativeHistory = representative.get(id);

            Double initial = 0.0;
            Double eventual = 0.0;
            Double increaseCount = 0.0;
            Double decreasedCount = 0.0;
            if (startDate != null && endDate != null) {
                ProductHistory lastInRange = productHistoryRepository.findLastInRange(id, endDate);
                ProductHistory firstInRange = productHistoryRepository.findFirstBeforeRange(id, startDate);
                increaseCount = getSumOfIncreasedCount(id, startDate, endDate);
                decreasedCount = getSumOfDecreasedCount(id, startDate, endDate);
                if (lastInRange != null && lastInRange.getQuantityResult() != null) {
                    eventual = lastInRange.getQuantityResult();
                }
                if (firstInRange != null && firstInRange.getQuantityResult() != null) {
                    initial = firstInRange.getQuantityResult();
                }
            }

            ProductHistoryResponse response = productHistoryMapper.toResponse(representativeHistory != null ? representativeHistory : productHistoryRepository.findLatestByProductId(id));
            response.setIncreasedCount(increaseCount);
            response.setDecreasedCount(decreasedCount);
            response.setInitialCount(initial);
            response.setEventualCount(eventual);
            return response;
        }).collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) totalProducts / (double) safeSize);
        PaginationMetadata metadata = PaginationMetadata.builder()
                .page(safePage)
                .size(safeSize)
                .totalElements(totalProducts)
                .totalPages(totalPages)
                .last(safePage >= totalPages - 1)
                .filter(filters)
                .sortDir(safeSortDir)
                .sortBy(safeSortBy)
                .table("productHistoryTable")
                .build();

        return Pair.of(responses, metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getTotalForProduct(Long productId) {
        Double total = productBalanceRepository.sumBalanceByProductId(productId);
        return total != null ? total : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getInitialForProduct(Long productId, LocalDateTime start) {
        ProductHistory first = productHistoryRepository.findFirstBeforeRange(productId, start);
        return first != null ? first.getQuantityResult() : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getEventualForProduct(Long productId, LocalDateTime end) {
        ProductHistory last = productHistoryRepository.findLastInRange(productId, end);
        return last != null ? last.getQuantityResult() : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getEventualForProduct(Long productId, Long warehouseId, LocalDateTime end) {
        ProductHistory last = productHistoryRepository.findLastInRangeWithWarehouseId(productId, warehouseId, end);
        return last != null ? last.getQuantityResult() : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getSumOfIncreasedCount(Long id, LocalDateTime start, LocalDateTime end) {
        return productHistoryRepository.sumIncreasedCount(id, start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getSumOfDecreasedCount(Long id, LocalDateTime start, LocalDateTime end) {
        return productHistoryRepository.sumDecreasedCount(id, start, end);
    }
}