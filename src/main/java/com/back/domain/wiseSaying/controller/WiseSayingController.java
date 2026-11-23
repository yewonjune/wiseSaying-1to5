package com.back.domain.wiseSaying.controller;

import com.back.AppContext;
import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.domain.wiseSaying.service.WiseSayingService;
import com.back.global.rq.Rq;
import com.back.standard.dto.Page;

import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// 명언 관련 응대 (입력·출력은 여기서만)
public class WiseSayingController {
    private final Scanner scanner = AppContext.scanner;
    private final WiseSayingService wiseSayingService = AppContext.wiseSayingService;

    public void actionWrite() {
        System.out.print("명언 : ");
        String content = scanner.nextLine().trim();

        System.out.print("작가 : ");
        String author = scanner.nextLine().trim();

        WiseSaying wiseSaying = wiseSayingService.write(content, author);

        System.out.println("%d번 명언이 등록되었습니다.".formatted(wiseSaying.getId()));
    }

    public void actionList(Rq rq) {
        String keywordType = rq.getParam("keywordType", "content");
        String keyword = rq.getParam("keyword", "");
        int page = rq.getParamAsInt("page", 1);

        if (!keyword.isEmpty()) {
            System.out.println("----------------------");
            System.out.println("검색타입 : " + keywordType);
            System.out.println("검색어 : " + keyword);
            System.out.println("----------------------");
        }

        Page<WiseSaying> wiseSayingPage = wiseSayingService.findForList(keywordType, keyword, page);

        System.out.println("번호 / 작가 / 명언");
        System.out.println("----------------------");

        wiseSayingPage.content().forEach(wiseSaying -> System.out.println("%d / %s / %s".formatted(
                wiseSaying.getId(),
                wiseSaying.getAuthor(),
                wiseSaying.getContent()
        )));

        System.out.println("----------------------");
        System.out.println("페이지 : " + pageMenu(wiseSayingPage));
    }

    // "1 / [2] / 3" 처럼 현재 페이지를 [ ] 로 표시
    private static String pageMenu(Page<?> page) {
        return IntStream.rangeClosed(1, page.totalPages())
                .mapToObj(i -> i == page.page() ? "[" + i + "]" : String.valueOf(i))
                .collect(Collectors.joining(" / "));
    }

    public void actionDelete(Rq rq) {
        int id = rq.getParamAsInt("id", -1);

        if (id == -1) {
            System.out.println("id를 숫자로 입력해주세요. (예: 삭제?id=1)");
            return;
        }

        if (!wiseSayingService.delete(id)) {
            System.out.println("%d번 명언은 존재하지 않습니다.".formatted(id));
            return;
        }

        System.out.println("%d번 명언이 삭제되었습니다.".formatted(id));
    }

    public void actionModify(Rq rq) {
        int id = rq.getParamAsInt("id", -1);

        if (id == -1) {
            System.out.println("id를 숫자로 입력해주세요. (예: 수정?id=1)");
            return;
        }

        Optional<WiseSaying> opWiseSaying = wiseSayingService.findById(id);

        if (opWiseSaying.isEmpty()) {
            System.out.println("%d번 명언은 존재하지 않습니다.".formatted(id));
            return;
        }

        WiseSaying wiseSaying = opWiseSaying.get();

        System.out.println("명언(기존) : " + wiseSaying.getContent());
        System.out.print("명언 : ");
        String content = scanner.nextLine().trim();

        System.out.println("작가(기존) : " + wiseSaying.getAuthor());
        System.out.print("작가 : ");
        String author = scanner.nextLine().trim();

        wiseSayingService.modify(wiseSaying, content, author);
    }
}
