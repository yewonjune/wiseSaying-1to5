package com.back;

import java.nio.file.Path;

// 저장 폴더 설정. 실제 실행은 db/wiseSaying, 테스트는 setTestMode() 로 테스트 전용 폴더를 쓴다
public class AppConfig {
    private static final Path PROD_DB_DIR = Path.of("db", "wiseSaying");
    private static final Path TEST_DB_DIR = Path.of("db", "test", "wiseSaying");

    private static Path dbDir = PROD_DB_DIR;

    public static Path getDbDir() {
        return dbDir;
    }

    public static void setProdMode() {
        dbDir = PROD_DB_DIR;
    }

    public static void setTestMode() {
        dbDir = TEST_DB_DIR;
    }
}
