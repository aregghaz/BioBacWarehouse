package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientHistory;
import com.biobac.warehouse.mapper.IngredientHistoryMapper;
import com.biobac.warehouse.repository.IngredientBalanceRepository;
import com.biobac.warehouse.repository.IngredientHistoryRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.IngredientHistoryResponse;
import com.biobac.warehouse.response.IngredientHistorySingleResponse;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.utils.DateUtil;
import com.biobac.warehouse.utils.GroupUtil;
import com.biobac.warehouse.utils.specifications.IngredientHistorySpecification;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngredientHistoryServiceImpl implements IngredientHistoryService {

    private final IngredientHistoryRepository ingredientHistoryRepository;
    private final IngredientHistoryMapper ingredientHistoryMapper;
    private final IngredientBalanceRepository ingredientBalanceRepository;
    private final GroupUtil groupUtil;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "timestamp";
    private static final String DEFAULT_SORT_DIR = "desc";

    private Pageable buildPageable(Integer page, Integer size, String sortBy, String sortDir) {
        int safePage = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int safeSize = (size == null || size <= 0) ? DEFAULT_SIZE : size;
        if (safeSize > 1000) safeSize = 1000;

        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? DEFAULT_SORT_BY : sortBy.trim();
        String safeSortDir = (sortDir == null || sortDir.isBlank()) ? DEFAULT_SORT_DIR : sortDir.trim();

        String mappedSortBy = mapSortField(safeSortBy);

        Sort sort = safeSortDir.equalsIgnoreCase("asc")
                ? Sort.by(mappedSortBy).ascending()
                : Sort.by(mappedSortBy).descending();

        return PageRequest.of(safePage, safeSize, sort);
    }

    private String mapSortField(String sortBy) {
        return switch (sortBy) {
            case "ingredientName" -> "ingredient.name";
            case "ingredientGroupName" -> "ingredient.ingredientGroup.name";
            case "unitName" -> "ingredient.unit.name";
            default -> sortBy;
        };
    }

    @Override
    @Transactional
    public IngredientHistorySingleResponse recordQuantityChange(com.biobac.warehouse.dto.IngredientHistoryDto dto) {
        if (dto == null || dto.getIngredient() == null) {
            throw new IllegalArgumentException("Ingredient and dto are required");
        }
        Ingredient ingredient = dto.getIngredient();
        IngredientHistory history = new IngredientHistory();
        history.setIngredient(ingredient);
        history.setWarehouse(dto.getWarehouse() != null ? dto.getWarehouse() : ingredient.getDefaultWarehouse());
        Double change = dto.getQuantityChange() != null ? dto.getQuantityChange() : 0.0;
        history.setIncrease(change > 0);
        history.setQuantityChange(change);
        Double total = ingredientBalanceRepository.sumBalanceByIngredientId(ingredient.getId());
        history.setQuantityResult(total != null ? total : 0.0);
        history.setNotes(dto.getNotes());
        history.setCompanyId(dto.getLastCompanyId());
        history.setLastPrice(dto.getLastPrice());
        history.setTimestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now());
        if (dto.getAction() != null) {
            history.setAction(dto.getAction());
        }
        IngredientHistory savedHistory = ingredientHistoryRepository.save(history);
        return ingredientHistoryMapper.toSingleResponse(savedHistory);
    }


    @Override
    @Transactional(readOnly = true)
    public Pair<List<IngredientHistorySingleResponse>, PaginationMetadata> getHistoryForIngredient(Long ingredientId, Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        String safeSortDir = (sortDir == null || sortDir.isBlank()) ? "desc" : sortDir;

        Sort sort = safeSortDir.equalsIgnoreCase("asc") ? Sort.by(safeSortBy).ascending() : Sort.by(safeSortBy).descending();

        if (!safeSortBy.equalsIgnoreCase("id")) {
            sort = sort.and(Sort.by("id").descending());
        }

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        pageable.getSort().and(sort);

        Specification<IngredientHistory> spec = IngredientHistorySpecification.buildSpecification(filters)
                .and((root, query, cb) -> cb.equal(root.join("ingredient", JoinType.LEFT).get("id"), ingredientId));

        Page<IngredientHistory> ingredientHistoryPage = ingredientHistoryRepository.findAll(spec, pageable);

        List<IngredientHistorySingleResponse> content = ingredientHistoryPage.getContent()
                .stream()
                .map(ingredientHistoryMapper::toSingleResponse)
                .collect(Collectors.toList());

        String metaSortDir = pageable.getSort().toString().contains("ASC") ? "asc" : "desc";
        String metaSortBy = pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse("id");

        PaginationMetadata metadata = new PaginationMetadata(
                ingredientHistoryPage.getNumber(),
                ingredientHistoryPage.getSize(),
                ingredientHistoryPage.getTotalElements(),
                ingredientHistoryPage.getTotalPages(),
                ingredientHistoryPage.isLast(),
                filters,
                metaSortDir,
                metaSortBy,
                "ingredientHistoryTable"
        );

        return Pair.of(content, metadata);
    }

    @Transactional(readOnly = true)
    @Override
    public Pair<List<IngredientHistoryResponse>, PaginationMetadata> getAll(
            Map<String, FilterCriteria> filters,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir) {

        List<Long> ingredientGroupIds = groupUtil.getAccessibleIngredientGroupIds();

        int safePage = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int safeSize = (size == null || size <= 0) ? DEFAULT_SIZE : size;
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "ingredientName" : sortBy.trim();
        String safeSortDir = (sortDir == null || sortDir.isBlank()) ? DEFAULT_SORT_DIR : sortDir.trim();

        Specification<IngredientHistory> spec = IngredientHistorySpecification.buildSpecification(filters)
                .and(IngredientHistorySpecification.belongsToIngredientGroups(ingredientGroupIds));

        List<IngredientHistory> matching = ingredientHistoryRepository.findAll(spec);
        Map<Long, List<IngredientHistory>> historiesByIngredient = matching.stream()
                .collect(Collectors.groupingBy(h -> h.getIngredient().getId()));

        List<LocalDateTime> dates = parseDates(filters);

        LocalDateTime startDate = dates.get(0);
        LocalDateTime endDate = dates.get(1);

        Map<Long, IngredientHistory> representative = historiesByIngredient.entrySet().stream()
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


        List<Long> sortedIngredientIds = representative.keySet().stream().sorted((id1, id2) -> {
            IngredientHistory h1 = representative.get(id1);
            IngredientHistory h2 = representative.get(id2);
            int cmp;
            switch (safeSortBy) {
                case "ingredientName":
                    String n1 = h1 != null && h1.getIngredient() != null && h1.getIngredient().getName() != null ? h1.getIngredient().getName() : "";
                    String n2 = h2 != null && h2.getIngredient() != null && h2.getIngredient().getName() != null ? h2.getIngredient().getName() : "";
                    cmp = n1.compareToIgnoreCase(n2);
                    break;
                case "unitName":
                    String u1 = h1 != null && h1.getIngredient() != null && h1.getIngredient().getUnit() != null && h1.getIngredient().getUnit().getName() != null ? h1.getIngredient().getUnit().getName() : "";
                    String u2 = h2 != null && h2.getIngredient() != null && h2.getIngredient().getUnit() != null && h2.getIngredient().getUnit().getName() != null ? h2.getIngredient().getUnit().getName() : "";
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

        int totalIngredients = sortedIngredientIds.size();
        int fromIndex = Math.min(safePage * safeSize, totalIngredients);
        int toIndex = Math.min(fromIndex + safeSize, totalIngredients);
        List<Long> pageOfIds = sortedIngredientIds.subList(fromIndex, toIndex);

        List<IngredientHistoryResponse> responses = pageOfIds.stream().map(id -> {
            IngredientHistory representativeHistory = representative.get(id);

            Double initial = 0.0;
            Double eventual = 0.0;
            Double increaseCount = 0.0;
            Double decreasedCount = 0.0;
            if (startDate != null && endDate != null) {
                IngredientHistory lastInRange = ingredientHistoryRepository.findLastInRange(id, startDate, endDate);
                IngredientHistory firstInRange = ingredientHistoryRepository.findFirstBeforeRange(id, startDate);
                increaseCount = getSumOfIncreasedCount(id, filters);
                decreasedCount = getSumOfDecreasedCount(id, filters);
                if (lastInRange != null && lastInRange.getQuantityResult() != null) {
                    eventual = lastInRange.getQuantityResult();
                }
                if (firstInRange != null && firstInRange.getQuantityResult() != null) {
                    initial = firstInRange.getQuantityResult();
                }
            }

            IngredientHistoryResponse response = ingredientHistoryMapper.toResponse(representativeHistory != null ? representativeHistory : ingredientHistoryRepository.findLatestByIngredientId(id));
            response.setIncreasedCount(increaseCount);
            response.setDecreasedCount(decreasedCount);
            response.setInitialCount(initial);
            response.setEventualCount(eventual);
            return response;
        }).collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) totalIngredients / (double) safeSize);
        PaginationMetadata metadata = PaginationMetadata.builder()
                .page(safePage)
                .size(safeSize)
                .totalElements(totalIngredients)
                .totalPages(totalPages)
                .last(safePage >= totalPages - 1)
                .filter(filters)
                .sortDir(safeSortDir)
                .sortBy(safeSortBy)
                .table("ingredientHistoryTable")
                .build();

        return Pair.of(responses, metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<IngredientHistorySingleResponse>, PaginationMetadata> getHistory(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        int safePage = (page == null || page < 0) ? 0 : page;
        int safeSize = (size == null || size <= 0) ? 20 : size;
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        String safeSortDir = (sortDir == null || sortDir.isBlank()) ? "desc" : sortDir;

        Sort sort = safeSortDir.equalsIgnoreCase("asc") ? Sort.by(safeSortBy).ascending() : Sort.by(safeSortBy).descending();
        Pageable pageable = PageRequest.of(safePage, safeSize, sort);

        Specification<IngredientHistory> spec = IngredientHistorySpecification.buildSpecification(filters);
        Page<IngredientHistory> ingredientHistoryPage = ingredientHistoryRepository.findAll(spec, pageable);

        List<IngredientHistorySingleResponse> content = ingredientHistoryPage.getContent()
                .stream()
                .map(ingredientHistoryMapper::toSingleResponse)
                .collect(Collectors.toList());

        String metaSortDir = pageable.getSort().toString().contains("ASC") ? "asc" : "desc";
        String metaSortBy = pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse("id");

        PaginationMetadata metadata = new PaginationMetadata(
                ingredientHistoryPage.getNumber(),
                ingredientHistoryPage.getSize(),
                ingredientHistoryPage.getTotalElements(),
                ingredientHistoryPage.getTotalPages(),
                ingredientHistoryPage.isLast(),
                filters,
                metaSortDir,
                metaSortBy,
                "ingredientsHistoryTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getTotalForIngredient(Long ingredientId) {
        Double total = ingredientBalanceRepository.sumBalanceByIngredientId(ingredientId);
        return total != null ? total : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getInitialForIngredient(Long ingredientId, Map<String, FilterCriteria> filters) {
        List<LocalDateTime> dates = parseDates(filters);
        IngredientHistory firstInRange = ingredientHistoryRepository.findFirstBeforeRange(ingredientId, dates.get(0));
        return firstInRange != null ? firstInRange.getQuantityResult() : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getEventualForIngredient(Long ingredientId, Map<String, FilterCriteria> filters) {
        List<LocalDateTime> dates = parseDates(filters);
        IngredientHistory firstInRange = ingredientHistoryRepository.findLastInRange(ingredientId, dates.get(0), dates.get(1));
        return firstInRange.getQuantityResult();
    }

    @Transactional(readOnly = true)
    @Override
    public Double getSumOfIncreasedCount(Long id, Map<String, FilterCriteria> filters) {
        List<LocalDateTime> dates = parseDates(filters);
        return ingredientHistoryRepository.sumIncreasedCount(id, dates.get(0), dates.get(1));
    }

    @Transactional(readOnly = true)
    @Override
    public Double getSumOfDecreasedCount(Long id, Map<String, FilterCriteria> filters) {
        List<LocalDateTime> dates = parseDates(filters);
        return ingredientHistoryRepository.sumDecreasedCount(id, dates.get(0), dates.get(1));
    }

    private List<LocalDateTime> parseDates(Map<String, FilterCriteria> filters) {
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        if (filters != null) {
            FilterCriteria ts = filters.get("timestamp");
            if (ts != null && ts.getOperator() != null) {
                String op = ts.getOperator();
                Object val = ts.getValue();
                try {
                    if ("between".equals(op) && val instanceof List<?> list && list.size() == 2) {
                        startDate = DateUtil.parseDateTime(String.valueOf(list.get(0)));
                        endDate = DateUtil.parseDateTime(String.valueOf(list.get(1)));
                    }
                } catch (Exception ignore) {
                }
            }
        }
        return List.of(startDate, endDate);
    }
}