package com.back.global.db;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

// DB 접속 정보. 소스에 적지 않고 환경변수 → db.properties 파일 순서로 읽는다
public record DbConfig(String host, int port, String username, String password, String dbName) {
    public static final Path PROPERTIES_FILE = Path.of("db.properties");
    private static final String ENV_PREFIX = "WISE_SAYING_DB_";

    public String jdbcUrl() {
        // createDatabaseIfNotExist: DB 가 없으면 접속하면서 만든다
        return "jdbc:mysql://%s:%d/%s?createDatabaseIfNotExist=true&characterEncoding=UTF-8"
                .formatted(host, port, dbName);
    }

    public static DbConfig load(boolean testMode) {
        Properties props = readPropertiesFile();

        String dbName = testMode
                ? get(props, "testName", "TEST_NAME", "wiseSaying_test")
                : get(props, "name", "NAME", "wiseSaying_dev");

        String username = get(props, "username", "USERNAME", null);
        String password = get(props, "password", "PASSWORD", null);

        if (username == null || password == null) {
            throw new IllegalStateException(
                    "DB 접속 정보가 없습니다. db.properties.example 을 db.properties 로 복사해 채우거나 "
                            + ENV_PREFIX + "USERNAME / " + ENV_PREFIX + "PASSWORD 환경변수를 설정하세요."
            );
        }

        return new DbConfig(
                get(props, "host", "HOST", "localhost"),
                Integer.parseInt(get(props, "port", "PORT", "3306")),
                username,
                password,
                dbName
        );
    }

    // 환경변수(WISE_SAYING_DB_xxx)가 있으면 그것을, 없으면 db.properties 의 db.xxx 를, 둘 다 없으면 기본값
    private static String get(Properties props, String key, String envSuffix, String defaultValue) {
        String env = System.getenv(ENV_PREFIX + envSuffix);
        if (env != null && !env.isBlank()) return env.trim();

        return props.getProperty("db." + key, defaultValue);
    }

    private static Properties readPropertiesFile() {
        Properties props = new Properties();

        if (!Files.exists(PROPERTIES_FILE)) return props;

        try (Reader reader = Files.newBufferedReader(PROPERTIES_FILE, StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return props;
    }
}
