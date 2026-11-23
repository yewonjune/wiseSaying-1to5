package com.back;

import com.back.domain.system.controller.SystemController;
import com.back.domain.wiseSaying.controller.WiseSayingController;
import com.back.global.rq.Rq;

import java.util.Scanner;

// 입력을 받아 알맞은 컨트롤러로 넘기기만 한다
public class App {
    public static final int SAMPLE_DATA_COUNT = 10;

    private final Scanner scanner;
    private final boolean makeSampleData;
    private final SystemController systemController;
    private final WiseSayingController wiseSayingController;

    // 샘플 없이 빈 저장소로 시작 (테스트용 기본값)
    public App(Scanner scanner) {
        this(scanner, false);
    }

    // App 을 만들 때마다 공유 객체(저장소·번호 포함)를 새로 만든다 → 테스트마다 초기화된다
    public App(Scanner scanner, boolean makeSampleData) {
        AppContext.init(scanner);

        this.scanner = AppContext.scanner;
        this.makeSampleData = makeSampleData;
        this.systemController = AppContext.systemController;
        this.wiseSayingController = AppContext.wiseSayingController;
    }

    public void run() {
        if (makeSampleData) {
            AppContext.wiseSayingService.makeSampleDataIfEmpty(SAMPLE_DATA_COUNT);
        }

        System.out.println("== 명언 앱 ==");

        while (true) {
            System.out.print("명령) ");

            if (!scanner.hasNextLine()) break; // 입력이 끝나면 종료

            Rq rq = new Rq(scanner.nextLine());

            switch (rq.getActionName()) {
                case "종료" -> {
                    systemController.actionExit();
                    return;
                }
                case "등록" -> wiseSayingController.actionWrite();
                case "목록" -> wiseSayingController.actionList(rq);
                case "삭제" -> wiseSayingController.actionDelete(rq);
                case "수정" -> wiseSayingController.actionModify(rq);
            }
        }
    }
}
