package com.biobac.warehouse.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageUtil {

  private PageUtil() {}

  public static final int DEFAULT_PAGE = 0;
  public static final int DEFAULT_SIZE = 20;
  public static final String DEFAULT_SORT_BY = "id";
  public static final String DEFAULT_SORT_DIR = "desc";

  public static Pageable buildPageable(Integer page, Integer size, String sortBy, String sortDir) {
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

  private static String mapSortField(String sortBy) {
    return switch (sortBy) {
      case "ingredientName" -> "ingredient.name";
      case "unitName" -> "ingredient.unit.name";
      case "status" -> "status.name";
      default -> sortBy;
    };
  }
}
