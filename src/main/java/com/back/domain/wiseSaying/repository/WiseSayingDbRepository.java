package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.global.db.DbConfig;
import com.back.standard.dto.Page;
import com.back.standard.util.Json;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// MySQL 저장소 (JDBC). wise_saying 테이블에 저장하고, 검색·페이징은 SQL 로 처리한다
public class WiseSayingDbRepository implements WiseSayingRepository {
    public static final String BUILD_FILE_NAME = "data.json";
    private static final String SCHEMA_RESOURCE = "/sql/schema.sql";

    private final DbConfig dbConfig;
    private final Path buildDir;

    public WiseSayingDbRepository(DbConfig dbConfig, Path buildDir) {
        this.dbConfig = dbConfig;
        this.buildDir = buildDir;

        createTableIfNotExists();
    }

    @Override
    public WiseSaying save(WiseSaying wiseSaying) {
        if (wiseSaying.isNew()) {
            long id = insert(
                    "INSERT INTO wise_saying (content, author) VALUES (?, ?)",
                    wiseSaying.getContent(), wiseSaying.getAuthor()
            );
            wiseSaying.setId((int) id);
        } else {
            update(
                    "UPDATE wise_saying SET content = ?, author = ? WHERE id = ?",
                    wiseSaying.getContent(), wiseSaying.getAuthor(), wiseSaying.getId()
            );
        }

        return wiseSaying;
    }

    @Override
    public int count() {
        return (int) selectLong("SELECT COUNT(*) FROM wise_saying");
    }

    @Override
    public List<WiseSaying> findAll() {
        return selectWiseSayings("SELECT id, content, author FROM wise_saying ORDER BY id ASC");
    }

    @Override
    public Optional<WiseSaying> findById(int id) {
        return selectWiseSayings("SELECT id, content, author FROM wise_saying WHERE id = ?", id)
                .stream()
                .findFirst();
    }

    @Override
    public boolean delete(int id) {
        return update("DELETE FROM wise_saying WHERE id = ?", id) > 0;
    }

    // 최신순(ORDER BY id DESC), 검색은 WHERE ... LIKE, 페이징은 LIMIT/OFFSET
    @Override
    public Page<WiseSaying> findForList(String keywordType, String keyword, int page, int pageSize) {
        String where = "";
        List<Object> params = new ArrayList<>();

        if (!keyword.isBlank()) {
            String column = keywordType.equals("author") ? "author" : "content";
            where = " WHERE " + column + " LIKE CONCAT('%', ?, '%')";
            params.add(escapeLike(keyword));
        }

        int totalCount = (int) selectLong("SELECT COUNT(*) FROM wise_saying" + where, params.toArray());
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));
        int currentPage = Math.clamp(page, 1, totalPages);

        params.add(pageSize);
        params.add((currentPage - 1) * pageSize);

        List<WiseSaying> content = selectWiseSayings(
                "SELECT id, content, author FROM wise_saying" + where + " ORDER BY id DESC LIMIT ? OFFSET ?",
                params.toArray()
        );

        return new Page<>(content, currentPage, pageSize, totalCount);
    }

    @Override
    public String build() {
        List<String> items = findAll().stream()
                .map(wiseSaying -> Json.toObject(Map.of(
                        "id", wiseSaying.getId(),
                        "content", wiseSaying.getContent(),
                        "author", wiseSaying.getAuthor()
                ), List.of("id", "content", "author")))
                .toList();

        try {
            Files.createDirectories(buildDir);
            Files.writeString(buildDir.resolve(BUILD_FILE_NAME), Json.toArray(items), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return BUILD_FILE_NAME;
    }

    // 테스트용: 테이블을 비우고 번호(AUTO_INCREMENT)도 1부터 다시 시작
    public void truncate() {
        update("TRUNCATE wise_saying");
    }

    private void createTableIfNotExists() {
        try (InputStream in = WiseSayingDbRepository.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (in == null) throw new IllegalStateException(SCHEMA_RESOURCE + " 를 찾을 수 없습니다.");

            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8)
                    .lines()
                    .filter(line -> !line.trim().startsWith("--"))
                    .reduce("", (a, b) -> a + "\n" + b)
                    .trim()
                    .replaceAll(";$", "");

            update(sql);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // LIKE 의 특수문자(\ % _)를 글자 그대로 찾도록 이스케이프
    private static String escapeLike(String keyword) {
        return keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    // ---- JDBC ----

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbConfig.jdbcUrl(), dbConfig.username(), dbConfig.password());
    }

    private static void bind(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    private int update(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SQL 실행 실패: " + sql, e);
        }
    }

    private long insert(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new IllegalStateException("생성된 번호를 받지 못했습니다.");
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL 실행 실패: " + sql, e);
        }
    }

    private long selectLong(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL 실행 실패: " + sql, e);
        }
    }

    private List<WiseSaying> selectWiseSayings(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                List<WiseSaying> list = new ArrayList<>();

                while (rs.next()) {
                    list.add(new WiseSaying(rs.getInt("id"), rs.getString("content"), rs.getString("author")));
                }

                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL 실행 실패: " + sql, e);
        }
    }
}
