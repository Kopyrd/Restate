package com.example.restate.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Generic response class for paginated data
 * @param <T> Type of data in the page
 */
@Data
@Builder
public class PageResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
}