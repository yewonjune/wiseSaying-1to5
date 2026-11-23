package com.back;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

// 실제 db/ 가 아닌 테스트 전용 폴더(db/test/wiseSaying)를 쓰고, 매 테스트 전에 비운다
// → 실행 순서와 상관없이 결과가 같다
class WiseSayingControllerTest {
    @BeforeAll
    static void beforeAll() {
        AppConfig.setTestMode();
    }

    @BeforeEach
    void beforeEach() throws IOException {
        deleteRecursively(AppConfig.getDbDir());
    }

    private static void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) return;

        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }

    private static String readDbFile(String fileName) throws IOException {
        return Files.readString(AppConfig.getDbDir().resolve(fileName)).replace("\r\n", "\n");
    }

    @Test
    @DisplayName("종료")
    void t1() {
        String out = AppTestRunner.run("""
                종료
                """);

        assertThat(out).contains("== 명언 앱 ==", "명령) ");
    }

    @Test
    @DisplayName("등록 시 명언과 작가를 입력받는다")
    void t2() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                종료
                """);

        assertThat(out).contains("명언 :", "작가 :");
    }

    @Test
    @DisplayName("등록")
    void t3() {
        // App must create a new empty repository for this test.
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                종료
                """);
        assertThat(out).contains("명언 :", "작가 :", "1번 명언이 등록되었습니다.");
    }

    @Test
    @DisplayName("등록할 때마다 번호가 1씩 증가한다")
    void t4() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                종료
                """);

        assertThat(out).contains("1번 명언이 등록되었습니다.", "2번 명언이 등록되었습니다.");
    }

    @Test
    @DisplayName("목록: 헤더와 함께 최신순으로 출력한다")
    void t5() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                목록
                종료
                """);

        assertThat(out)
                .contains("번호 / 작가 / 명언", "----------------------")
                .containsSubsequence(
                        "2 / 작자미상 / 과거에 집착하지 마라.",
                        "1 / 작자미상 / 현재를 사랑하라."
                );
    }

    @Test
    @DisplayName("삭제?id=1: 삭제하고 목록에서 사라진다")
    void t6() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                삭제?id=1
                목록
                종료
                """);

        assertThat(out)
                .contains("1번 명언이 삭제되었습니다.", "2 / 작자미상 / 과거에 집착하지 마라.")
                .doesNotContain("1 / 작자미상 / 현재를 사랑하라.");
    }

    @Test
    @DisplayName("없는 번호 삭제: 존재하지 않는다고 안내한다")
    void t7() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                삭제?id=1
                삭제?id=1
                종료
                """);

        assertThat(out).contains("1번 명언이 삭제되었습니다.", "1번 명언은 존재하지 않습니다.");
    }

    @Test
    @DisplayName("수정?id=2: 기존 값을 보여 주고 새 값으로 바꾼다")
    void t8() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                수정?id=2
                현재와 자신을 사랑하라.
                홍길동
                목록
                종료
                """);

        assertThat(out)
                .contains(
                        "명언(기존) : 과거에 집착하지 마라.",
                        "작가(기존) : 작자미상",
                        "2 / 홍길동 / 현재와 자신을 사랑하라."
                )
                .doesNotContain("2 / 작자미상 / 과거에 집착하지 마라.");
    }

    @Test
    @DisplayName("없는 번호 수정: 존재하지 않는다고 안내한다")
    void t9() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                수정?id=3
                종료
                """);

        assertThat(out)
                .contains("3번 명언은 존재하지 않습니다.")
                .doesNotContain("명언(기존) :");
    }

    @Test
    @DisplayName("삭제된 번호는 재사용하지 않는다")
    void t10() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                삭제?id=1
                등록
                과거에 집착하지 마라.
                작자미상
                종료
                """);

        assertThat(out).contains("2번 명언이 등록되었습니다.");
    }

    @Test
    @DisplayName("앱을 다시 시작해도 명언이 남고 번호가 이어진다")
    void t11() {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                종료
                """);

        String out = AppTestRunner.run("""
                등록
                과거에 집착하지 마라.
                작자미상
                목록
                종료
                """);

        assertThat(out)
                .contains("2번 명언이 등록되었습니다.")
                .containsSubsequence(
                        "2 / 작자미상 / 과거에 집착하지 마라.",
                        "1 / 작자미상 / 현재를 사랑하라."
                );
    }

    // ---- 13단계: 검색 ----

    @Test
    @DisplayName("목록?keywordType=content&keyword=과거: 명언 내용으로 검색하고 검색 조건을 위에 보여 준다")
    void t12() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                목록?keywordType=content&keyword=과거
                종료
                """);

        assertThat(out)
                .containsSubsequence(
                        "----------------------",
                        "검색타입 : content",
                        "검색어 : 과거",
                        "----------------------",
                        "번호 / 작가 / 명언",
                        "----------------------",
                        "2 / 작자미상 / 과거에 집착하지 마라."
                )
                .doesNotContain("1 / 작자미상 / 현재를 사랑하라.");
    }

    @Test
    @DisplayName("목록?keywordType=author&keyword=작자: 작가로 검색한다")
    void t13() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                홍길동
                목록?keywordType=author&keyword=작자
                종료
                """);

        assertThat(out)
                .contains("검색타입 : author", "검색어 : 작자", "1 / 작자미상 / 현재를 사랑하라.")
                .doesNotContain("2 / 홍길동 / 과거에 집착하지 마라.");
    }

    @Test
    @DisplayName("검색어가 없으면 검색 조건을 보여 주지 않는다")
    void t14() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                목록
                종료
                """);

        assertThat(out)
                .contains("1 / 작자미상 / 현재를 사랑하라.")
                .doesNotContain("검색타입 :", "검색어 :");
    }

    // ---- 14단계: 페이징 (샘플 10개로 시작) ----

    private static String runWithSampleData(String input) {
        return AppTestRunner.run(input, scanner -> new App(scanner, true).run());
    }

    @Test
    @DisplayName("명언이 0개면 샘플 10개를 만들고, 목록은 1페이지(10~6번)를 최신순으로 보여 준다")
    void t15() {
        String out = runWithSampleData("""
                목록
                종료
                """);

        assertThat(out)
                .containsSubsequence(
                        "번호 / 작가 / 명언",
                        "----------------------",
                        "10 / 작자미상 10 / 명언 10",
                        "9 / 작자미상 9 / 명언 9",
                        "8 / 작자미상 8 / 명언 8",
                        "7 / 작자미상 7 / 명언 7",
                        "6 / 작자미상 6 / 명언 6",
                        "----------------------",
                        "페이지 : [1] / 2"
                )
                .doesNotContain("5 / 작자미상 5 / 명언 5");
    }

    @Test
    @DisplayName("목록?page=2: 2페이지(5~1번)를 보여 주고 현재 페이지를 [ ] 로 표시한다")
    void t16() {
        String out = runWithSampleData("""
                목록?page=2
                종료
                """);

        assertThat(out)
                .containsSubsequence(
                        "5 / 작자미상 5 / 명언 5",
                        "4 / 작자미상 4 / 명언 4",
                        "3 / 작자미상 3 / 명언 3",
                        "2 / 작자미상 2 / 명언 2",
                        "1 / 작자미상 1 / 명언 1",
                        "페이지 : 1 / [2]"
                )
                .doesNotContain("6 / 작자미상 6 / 명언 6");
    }

    @Test
    @DisplayName("내용에 1 검색: 10번과 1번만 나온다")
    void t17() {
        String out = runWithSampleData("""
                목록?keywordType=content&keyword=1
                종료
                """);

        assertThat(out)
                .containsSubsequence(
                        "10 / 작자미상 10 / 명언 10",
                        "1 / 작자미상 1 / 명언 1",
                        "페이지 : [1]"
                )
                .doesNotContain("2 / 작자미상 2 / 명언 2", "9 / 작자미상 9 / 명언 9", "[1] / 2");
    }

    @Test
    @DisplayName("검색과 page 를 함께 주면 검색 결과를 기준으로 페이징한다")
    void t18() {
        String out = runWithSampleData("""
                등록
                다른 명언
                홍길동
                목록?keywordType=author&keyword=작자미상&page=2
                종료
                """);

        // 작자미상 명언은 10개 → 2페이지는 5~1번, 홍길동(11번)은 검색에서 빠진다
        assertThat(out)
                .containsSubsequence(
                        "5 / 작자미상 5 / 명언 5",
                        "1 / 작자미상 1 / 명언 1",
                        "페이지 : 1 / [2]"
                )
                .doesNotContain("11 / 홍길동 / 다른 명언", "페이지 : 1 / [2] / 3");
    }

    @Test
    @DisplayName("CRUD 테스트는 샘플 없이 빈 저장소로 시작한다")
    void t19() {
        String out = AppTestRunner.run("""
                목록
                종료
                """);

        assertThat(out)
                .contains("페이지 : [1]")
                .doesNotContain("작자미상 10", "/ 2");
    }

    // ---- 9단계: 파일 영속성 ----

    @Test
    @DisplayName("등록하면 {번호}.json 과 lastId.txt 가 생긴다")
    void t20() throws IOException {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                종료
                """);

        assertThat(readDbFile("1.json")).isEqualTo("""
                {
                  "id": 1,
                  "content": "현재를 사랑하라.",
                  "author": "작자미상"
                }""");
        assertThat(readDbFile("lastId.txt")).isEqualTo("1");
    }

    @Test
    @DisplayName("프로그램을 다시 시작해도 파일에서 읽어 목록에 보여 준다")
    void t21() {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                종료
                """);

        String out = AppTestRunner.run("""
                목록
                종료
                """);

        assertThat(out).containsSubsequence(
                "번호 / 작가 / 명언",
                "----------------------",
                "2 / 작자미상 / 과거에 집착하지 마라.",
                "1 / 작자미상 / 현재를 사랑하라."
        );
    }

    @Test
    @DisplayName("수정하면 파일이 갱신되고, 재시작 후에도 수정된 내용이 보인다")
    void t22() throws IOException {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                수정?id=1
                현재와 자신을 사랑하라.
                홍길동
                종료
                """);

        assertThat(readDbFile("1.json")).contains(
                "\"content\": \"현재와 자신을 사랑하라.\"",
                "\"author\": \"홍길동\""
        );

        String out = AppTestRunner.run("""
                목록
                종료
                """);

        assertThat(out).contains("1 / 홍길동 / 현재와 자신을 사랑하라.");
    }

    @Test
    @DisplayName("삭제하면 파일도 지우고, 재시작 후 새 명언은 삭제된 번호를 재사용하지 않는다")
    void t23() {
        AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                삭제?id=2
                종료
                """);

        assertThat(AppConfig.getDbDir().resolve("2.json")).doesNotExist();
        assertThat(AppConfig.getDbDir().resolve("1.json")).exists();

        String out = AppTestRunner.run("""
                등록
                새 명언
                새 작가
                목록
                종료
                """);

        assertThat(out)
                .contains("3번 명언이 등록되었습니다.", "3 / 새 작가 / 새 명언", "1 / 작자미상 / 현재를 사랑하라.")
                .doesNotContain("2 / 작자미상 / 과거에 집착하지 마라.");
    }

    // ---- 10단계: 빌드 ----

    @Test
    @DisplayName("빌드: 모든 명언을 배열 형태의 data.json 으로 만들고 갱신 안내를 출력한다")
    void t24() throws IOException {
        String out = AppTestRunner.run("""
                등록
                명언 1
                작가 1
                등록
                명언 2
                작가 2
                빌드
                종료
                """);

        assertThat(out).contains("data.json 파일의 내용이 갱신되었습니다.");
        assertThat(readDbFile("data.json")).isEqualTo("""
                [
                  {
                    "id": 1,
                    "content": "명언 1",
                    "author": "작가 1"
                  },
                  {
                    "id": 2,
                    "content": "명언 2",
                    "author": "작가 2"
                  }
                ]""");
    }

    @Test
    @DisplayName("빌드 파일(data.json)과 lastId.txt 는 명언으로 읽지 않는다")
    void t25() {
        String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                빌드
                목록
                종료
                """);

        assertThat(out)
                .contains("1 / 작자미상 / 현재를 사랑하라.", "페이지 : [1]")
                .doesNotContain("2 /");
    }
}
