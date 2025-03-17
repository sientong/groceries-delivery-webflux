package com.sientong.groceries.api.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginatedResponse<T> {
    private List<T> content;
    private int number;         // Page number (0-based)
    private int size;          // Page size
    private long totalElements;
    private int totalPages;
    private boolean isFirst;     // Lombok will generate isFirst() and isFirst() builder method
    private boolean isLast;      // Lombok will generate isLast() and isLast() builder method
    private boolean isEmpty;     // Lombok will generate isEmpty() and isEmpty() builder method
}
