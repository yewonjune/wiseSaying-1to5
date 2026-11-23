package com.ll;

import java.util.Scanner;

public class App {
    private final Scanner scanner;

    // 5단계 힌트대로 배열에 저장하고, 가득 차면 두 배로 늘린다
    private WiseSaying[] wiseSayings = new WiseSaying[10];
    private int wiseSayingsSize = 0;

    // 마지막으로 발급한 번호 (번호는 재사용하지 않는다)
    private int lastId = 0;

    public App(Scanner scanner) {
        this.scanner = scanner;
    }

    public void run() {
        System.out.println("== 명언 앱 ==");

        while (true) {
            System.out.print("명령) ");

            if (!scanner.hasNextLine()) break; // 입력이 끝나면 종료

            String cmd = scanner.nextLine().trim();

            if (cmd.equals("종료")) {
                break;
            } else if (cmd.equals("등록")) {
                actionWrite();
            } else if (cmd.equals("목록")) {
                actionList();
            }
        }
    }

    private void actionWrite() {
        System.out.print("명언 : ");
        String content = scanner.nextLine().trim();

        System.out.print("작가 : ");
        String author = scanner.nextLine().trim();

        WiseSaying wiseSaying = write(content, author);

        System.out.println("%d번 명언이 등록되었습니다.".formatted(wiseSaying.getId()));
    }

    private void actionList() {
        System.out.println("번호 / 작가 / 명언");
        System.out.println("----------------------");

        // 최신순: 나중에 등록한 것부터
        for (int i = wiseSayingsSize - 1; i >= 0; i--) {
            WiseSaying wiseSaying = wiseSayings[i];

            System.out.println("%d / %s / %s".formatted(
                    wiseSaying.getId(),
                    wiseSaying.getAuthor(),
                    wiseSaying.getContent()
            ));
        }
    }

    private WiseSaying write(String content, String author) {
        WiseSaying wiseSaying = new WiseSaying(++lastId, content, author);

        if (wiseSayingsSize == wiseSayings.length) {
            WiseSaying[] bigger = new WiseSaying[wiseSayings.length * 2];
            System.arraycopy(wiseSayings, 0, bigger, 0, wiseSayingsSize);
            wiseSayings = bigger;
        }

        wiseSayings[wiseSayingsSize++] = wiseSaying;

        return wiseSaying;
    }
}
