package com.bloom.common.dto;

import java.util.Collections;
import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious
) {

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        List<T> safeContent = content != null ? content : Collections.emptyList();
        int safeSize = size > 0 ? size : 10;
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        boolean first = page <= 0;
        boolean last = page >= totalPages - 1 || totalPages == 0;
        boolean hasNext = page < totalPages - 1;
        boolean hasPrevious = page > 0;

        return new PageResponse<>(
                safeContent,
                page,
                safeSize,
                totalElements,
                totalPages,
                first,
                last,
                hasNext,
                hasPrevious
        );
    }

    public static <T> PageResponse<T> empty(int page, int size) {
        return of(Collections.emptyList(), page, size, 0);
    }
}
