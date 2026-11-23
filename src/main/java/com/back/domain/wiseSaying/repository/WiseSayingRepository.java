package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.standard.dto.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// 데이터 생성·조회·수정·삭제만 맡는다 (메모리 리스트)
public class WiseSayingRepository {
    private final List<WiseSaying> wiseSayings = new ArrayList<>();

    // 마지막으로 발급한 번호 (삭제된 번호는 재사용하지 않는다)
    private int lastId = 0;

    // 새 명언이면 번호를 붙여 추가하고, 이미 있는 명언이면 그대로 둔다(같은 객체를 수정했으므로)
    public WiseSaying save(WiseSaying wiseSaying) {
        if (wiseSaying.isNew()) {
            wiseSaying.setId(++lastId);
            wiseSayings.add(wiseSaying);
        }

        return wiseSaying;
    }

    public int count() {
        return wiseSayings.size();
    }

    // 최신순으로, 검색어가 있으면 keywordType(content/author)에 부분 일치하는 것만 골라 페이지로 자른다
    public Page<WiseSaying> findForList(String keywordType, String keyword, int page, int pageSize) {
        List<WiseSaying> filtered = wiseSayings.reversed()
                .stream()
                .filter(wiseSaying -> matches(wiseSaying, keywordType, keyword))
                .toList();

        return Page.of(filtered, page, pageSize);
    }

    private static boolean matches(WiseSaying wiseSaying, String keywordType, String keyword) {
        if (keyword.isBlank()) return true;

        return switch (keywordType) {
            case "author" -> wiseSaying.getAuthor().contains(keyword);
            default -> wiseSaying.getContent().contains(keyword);
        };
    }

    public Optional<WiseSaying> findById(int id) {
        return wiseSayings.stream()
                .filter(wiseSaying -> wiseSaying.getId() == id)
                .findFirst();
    }

    public boolean delete(int id) {
        return wiseSayings.removeIf(wiseSaying -> wiseSaying.getId() == id);
    }
}
