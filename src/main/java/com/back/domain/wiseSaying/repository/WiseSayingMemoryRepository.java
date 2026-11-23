package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// 메모리(리스트) 저장소: 프로그램을 끄면 사라진다
public class WiseSayingMemoryRepository implements WiseSayingRepository {
    private final List<WiseSaying> wiseSayings = new ArrayList<>();

    // 마지막으로 발급한 번호 (삭제된 번호는 재사용하지 않는다)
    private int lastId = 0;

    @Override
    public WiseSaying save(WiseSaying wiseSaying) {
        if (wiseSaying.isNew()) {
            wiseSaying.setId(++lastId);
            wiseSayings.add(wiseSaying);
        }

        return wiseSaying;
    }

    @Override
    public int count() {
        return wiseSayings.size();
    }

    @Override
    public List<WiseSaying> findAll() {
        return List.copyOf(wiseSayings);
    }

    @Override
    public Optional<WiseSaying> findById(int id) {
        return wiseSayings.stream()
                .filter(wiseSaying -> wiseSaying.getId() == id)
                .findFirst();
    }

    @Override
    public boolean delete(int id) {
        return wiseSayings.removeIf(wiseSaying -> wiseSaying.getId() == id);
    }

    @Override
    public String build() {
        throw new UnsupportedOperationException("메모리 저장소는 빌드를 지원하지 않습니다.");
    }
}
