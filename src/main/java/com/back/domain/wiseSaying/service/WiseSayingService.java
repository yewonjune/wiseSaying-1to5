package com.back.domain.wiseSaying.service;

import com.back.AppContext;
import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.domain.wiseSaying.repository.WiseSayingRepository;
import com.back.standard.dto.Page;

import java.util.Optional;

// 순수 비즈니스 로직 (입출력 없음)
public class WiseSayingService {
    public static final int PAGE_SIZE = 5;

    private final WiseSayingRepository wiseSayingRepository = AppContext.wiseSayingRepository;

    public WiseSaying write(String content, String author) {
        return wiseSayingRepository.save(new WiseSaying(content, author));
    }

    // 명언이 하나도 없을 때만 샘플을 만든다 (명언 1 / 작자미상 1 ~ 명언 count / 작자미상 count)
    public void makeSampleDataIfEmpty(int count) {
        if (wiseSayingRepository.count() > 0) return;

        for (int i = 1; i <= count; i++) {
            write("명언 " + i, "작자미상 " + i);
        }
    }

    // 최신순, 검색 결과 기준으로 페이징
    public Page<WiseSaying> findForList(String keywordType, String keyword, int page) {
        return wiseSayingRepository.findForList(keywordType, keyword, page, PAGE_SIZE);
    }

    public Optional<WiseSaying> findById(int id) {
        return wiseSayingRepository.findById(id);
    }

    public boolean delete(int id) {
        return wiseSayingRepository.delete(id);
    }

    public void modify(WiseSaying wiseSaying, String content, String author) {
        wiseSaying.setContent(content);
        wiseSaying.setAuthor(author);

        wiseSayingRepository.save(wiseSaying);
    }
}
