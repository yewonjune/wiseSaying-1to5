package com.back;

import com.back.domain.system.controller.SystemController;
import com.back.domain.wiseSaying.controller.WiseSayingController;
import com.back.global.rq.Rq;

import java.util.Scanner;

// 입력을 받아 알맞은 컨트롤러로 넘기기만 한다
public class App {
    private final Scanner scanner;
    private final SystemController systemController;
    private final WiseSayingController wiseSayingController;

    // App 을 만들 때마다 공유 객체(저장소·번호 포함)를 새로 만든다 → 테스트마다 초기화된다
    public App(Scanner scanner) {
        AppContext.init(scanner);

        this.scanner = AppContext.scanner;
        this.systemController = AppContext.systemController;
        this.wiseSayingController = AppContext.wiseSayingController;
    }

    public void run() {
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
                case "목록" -> wiseSayingController.actionList();
                case "삭제" -> wiseSayingController.actionDelete(rq);
                case "수정" -> wiseSayingController.actionModify(rq);
            }
        }
    }
}
