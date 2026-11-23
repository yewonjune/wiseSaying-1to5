package com.back.standard.dto;

import java.util.List;

// 한 페이지에 들어갈 항목과 페이지 정보
public record Page<T>(List<T> content, int page, int pageSize, int totalCount) {
    public int totalPages() {
        return Math.max(1, (int) Math.ceil((double) totalCount / pageSize));
    }

    // 전체 목록에서 page 번째(1부터) 페이지를 잘라 낸다. 범위를 벗어난 page 는 가까운 끝 페이지로 맞춘다
    public static <T> Page<T> of(List<T> all, int page, int pageSize) {
        int totalCount = all.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));
        int currentPage = Math.clamp(page, 1, totalPages);

        int from = (currentPage - 1) * pageSize;
        int to = Math.min(from + pageSize, totalCount);

        return new Page<>(all.subList(from, to), currentPage, pageSize, totalCount);
    }
}
