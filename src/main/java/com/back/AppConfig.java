package com.back;

import com.back.global.db.DbConfig;

import java.nio.file.Path;

// 실행 모드 설정. 실제 실행은 개발 DB·db/wiseSaying, 테스트는 setTestMode() 로 테스트 DB·테스트 전용 폴더를 쓴다
public class AppConfig {
    private static final Path PROD_DB_DIR = Path.of("db", "wiseSaying");
    private static final Path TEST_DB_DIR = Path.of("db", "test", "wiseSaying");

    private static boolean testMode = false;

    public static boolean isTestMode() {
        return testMode;
    }

    public static void setProdMode() {
        testMode = false;
    }

    public static void setTestMode() {
        testMode = true;
    }

    // 파일 저장소 폴더이자 빌드 결과(data.json)를 두는 폴더
    public static Path getDbDir() {
        return testMode ? TEST_DB_DIR : PROD_DB_DIR;
    }

    public static DbConfig getDbConfig() {
        return DbConfig.load(testMode);
    }
}
