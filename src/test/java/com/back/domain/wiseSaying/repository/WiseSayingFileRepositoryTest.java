package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

// 지금은 DB 저장소를 쓰지만, 파일 저장소도 같은 인터페이스로 계속 동작하는지 확인한다
class WiseSayingFileRepositoryTest {
    @TempDir
    Path dir;

    @Test
    @DisplayName("저장하면 {번호}.json 과 lastId.txt 가 생기고, 새 저장소로 다시 읽어도 남아 있다")
    void t1() throws IOException {
        new WiseSayingFileRepository(dir).save(new WiseSaying("현재를 사랑하라.", "작자미상"));

        assertThat(Files.readString(dir.resolve("1.json")).replace("\r\n", "\n")).isEqualTo("""
                {
                  "id": 1,
                  "content": "현재를 사랑하라.",
                  "author": "작자미상"
                }""");
        assertThat(Files.readString(dir.resolve("lastId.txt"))).isEqualTo("1");

        WiseSayingFileRepository reopened = new WiseSayingFileRepository(dir);
        assertThat(reopened.findById(1)).isPresent();
    }

    @Test
    @DisplayName("삭제하면 파일을 지우고, 삭제된 번호는 재사용하지 않는다")
    void t2() {
        WiseSayingFileRepository repository = new WiseSayingFileRepository(dir);
        repository.save(new WiseSaying("a", "b"));
        repository.save(new WiseSaying("c", "d"));

        assertThat(repository.delete(2)).isTrue();
        assertThat(dir.resolve("2.json")).doesNotExist();

        WiseSaying saved = new WiseSayingFileRepository(dir).save(new WiseSaying("e", "f"));
        assertThat(saved.getId()).isEqualTo(3);
    }
}
