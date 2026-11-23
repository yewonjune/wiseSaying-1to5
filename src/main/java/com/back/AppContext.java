package com.back;

import com.back.domain.system.controller.SystemController;
import com.back.domain.wiseSaying.controller.WiseSayingController;
import com.back.domain.wiseSaying.repository.WiseSayingDbRepository;
import com.back.domain.wiseSaying.repository.WiseSayingRepository;
import com.back.domain.wiseSaying.service.WiseSayingService;

import java.util.Scanner;

// 공유 객체를 한 곳에서 하나씩만 만들어 둔다
public class AppContext {
    public static Scanner scanner;
    public static WiseSayingRepository wiseSayingRepository;
    public static WiseSayingService wiseSayingService;
    public static WiseSayingController wiseSayingController;
    public static SystemController systemController;

    public static void init(Scanner scanner) {
        AppContext.scanner = scanner;
        // 저장소 구현체만 갈아 끼운다 (컨트롤러·서비스는 그대로)
        AppContext.wiseSayingRepository = new WiseSayingDbRepository(AppConfig.getDbConfig(), AppConfig.getDbDir());
        AppContext.wiseSayingService = new WiseSayingService();
        AppContext.wiseSayingController = new WiseSayingController();
        AppContext.systemController = new SystemController();
    }
}
