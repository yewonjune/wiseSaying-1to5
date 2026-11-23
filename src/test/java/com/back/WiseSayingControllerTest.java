package com.back;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// AppTestRunner.run() 은 매번 new App(scanner) 로 실행하므로,
// 테스트마다 저장소와 번호가 새로 초기화된다 (실행 순서와 상관없이 결과가 같다)
class WiseSayingControllerTest {
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
    @DisplayName("앱을 다시 실행하면 저장소와 번호가 초기화된다")
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
                .contains("1번 명언이 등록되었습니다.", "1 / 작자미상 / 과거에 집착하지 마라.")
                .doesNotContain("현재를 사랑하라.");
    }
}
