package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.standard.util.Json;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

// 파일 저장소: 명언 하나를 {dir}/{번호}.json 하나로, 마지막 번호는 {dir}/lastId.txt 에 저장한다
public class WiseSayingFileRepository implements WiseSayingRepository {
    public static final String BUILD_FILE_NAME = "data.json";

    private final Path dir;

    public WiseSayingFileRepository(Path dir) {
        this.dir = dir;
    }

    @Override
    public WiseSaying save(WiseSaying wiseSaying) {
        if (wiseSaying.isNew()) {
            int id = getLastId() + 1;
            wiseSaying.setId(id);
            writeString(lastIdPath(), String.valueOf(id));
        }

        writeString(jsonPath(wiseSaying.getId()), toJson(wiseSaying));

        return wiseSaying;
    }

    @Override
    public int count() {
        return findAll().size();
    }

    @Override
    public List<WiseSaying> findAll() {
        if (!Files.isDirectory(dir)) return List.of();

        try (Stream<Path> paths = Files.list(dir)) {
            return paths
                    .filter(path -> path.getFileName().toString().matches("\\d+\\.json"))
                    .map(WiseSayingFileRepository::readWiseSaying)
                    .sorted(Comparator.comparingInt(WiseSaying::getId))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<WiseSaying> findById(int id) {
        Path path = jsonPath(id);

        if (!Files.exists(path)) return Optional.empty();

        return Optional.of(readWiseSaying(path));
    }

    @Override
    public boolean delete(int id) {
        try {
            return Files.deleteIfExists(jsonPath(id));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String build() {
        List<String> items = findAll().stream()
                .map(WiseSayingFileRepository::toJson)
                .toList();

        writeString(dir.resolve(BUILD_FILE_NAME), Json.toArray(items));

        return BUILD_FILE_NAME;
    }

    public int getLastId() {
        Path path = lastIdPath();

        if (!Files.exists(path)) return 0;

        try {
            return Integer.parseInt(Files.readString(path, StandardCharsets.UTF_8).trim());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Path jsonPath(int id) {
        return dir.resolve(id + ".json");
    }

    private Path lastIdPath() {
        return dir.resolve("lastId.txt");
    }

    private static String toJson(WiseSaying wiseSaying) {
        return Json.toObject(Map.of(
                "id", wiseSaying.getId(),
                "content", wiseSaying.getContent(),
                "author", wiseSaying.getAuthor()
        ), List.of("id", "content", "author"));
    }

    private static WiseSaying readWiseSaying(Path path) {
        try {
            Map<String, Object> map = Json.parseObject(Files.readString(path, StandardCharsets.UTF_8));

            return new WiseSaying(
                    ((Number) map.get("id")).intValue(),
                    (String) map.get("content"),
                    (String) map.get("author")
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeString(Path path, String content) {
        try {
            Files.createDirectories(dir);
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
