package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.standard.dto.Page;

import java.util.List;
import java.util.Optional;

// 데이터 생성·조회·수정·삭제 (구현체를 갈아 끼울 수 있다: 메모리 / 파일)
public interface WiseSayingRepository {
    // 새 명언이면 번호를 붙여 저장하고, 이미 있는 명언이면 내용을 갱신한다
    WiseSaying save(WiseSaying wiseSaying);

    int count();

    // 번호 오름차순 전체
    List<WiseSaying> findAll();

    Optional<WiseSaying> findById(int id);

    boolean delete(int id);

    // 모든 명언을 모은 data.json 을 만들고, 만든 파일 이름을 돌려준다
    String build();

    // 최신순으로, 검색어가 있으면 keywordType(content/author)에 부분 일치하는 것만 골라 페이지로 자른다
    default Page<WiseSaying> findForList(String keywordType, String keyword, int page, int pageSize) {
        List<WiseSaying> filtered = findAll()
                .reversed()
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
}
