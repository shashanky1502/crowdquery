package com.crowdquery.crowdquery.dto;

import lombok.Data;

import java.util.List;

@Data
public class PaginatedResponseDto<T> {
    private List<T> data;
    private Pagination pagination;

    public PaginatedResponseDto(List<T> data, Pagination pagination) {
        this.data = data;
        this.pagination = pagination;
    }

    @Data
    public static class Pagination {
        private int limit;
        private int offset;
        private long total;
        private boolean hasMore;

        public Pagination(int limit, int offset, long total, boolean hasMore) {
            this.limit = limit;
            this.offset = offset;
            this.total = total;
            this.hasMore = hasMore;
        }
    }
}