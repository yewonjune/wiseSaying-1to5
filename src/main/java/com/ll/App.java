package com.ll;

import java.util.Scanner;

public class App {
    private final Scanner scanner;

    // 배열에 저장하고, 가득 차면 두 배로 늘린다
    private WiseSaying[] wiseSayings = new WiseSaying[10];
    private int wiseSayingsSize = 0;

    // 마지막으로 발급한 번호 (삭제된 번호는 재사용하지 않는다)
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
            String action = cmd.split("[?]", 2)[0];

            if (action.equals("종료")) {
                break;
            } else if (action.equals("등록")) {
                actionWrite();
            } else if (action.equals("목록")) {
                actionList();
            } else if (action.equals("삭제")) {
                actionDelete(cmd);
            } else if (action.equals("수정")) {
                actionModify(cmd);
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

    private void actionDelete(String cmd) {
        int id = parseId(cmd);
        if (id == -1) return;

        if (!delete(id)) {
            System.out.println("%d번 명언은 존재하지 않습니다.".formatted(id));
            return;
        }

        System.out.println("%d번 명언이 삭제되었습니다.".formatted(id));
    }

    private void actionModify(String cmd) {
        int id = parseId(cmd);
        if (id == -1) return;

        int index = findIndexById(id);

        if (index == -1) {
            System.out.println("%d번 명언은 존재하지 않습니다.".formatted(id));
            return;
        }

        WiseSaying wiseSaying = wiseSayings[index];

        System.out.println("명언(기존) : " + wiseSaying.getContent());
        System.out.print("명언 : ");
        String content = scanner.nextLine().trim();

        System.out.println("작가(기존) : " + wiseSaying.getAuthor());
        System.out.print("작가 : ");
        String author = scanner.nextLine().trim();

        wiseSaying.setContent(content);
        wiseSaying.setAuthor(author);
    }

    // "삭제?id=1" 에서 id 값을 꺼낸다. 없거나 숫자가 아니면 안내하고 -1
    private int parseId(String cmd) {
        String[] cmdBits = cmd.split("[?]", 2);

        if (cmdBits.length == 2) {
            for (String param : cmdBits[1].split("&")) {
                String[] keyValue = param.split("=", 2);

                if (keyValue.length == 2 && keyValue[0].trim().equals("id")) {
                    try {
                        return Integer.parseInt(keyValue[1].trim());
                    } catch (NumberFormatException e) {
                        break;
                    }
                }
            }
        }

        System.out.println("id를 숫자로 입력해주세요. (예: %s?id=1)".formatted(cmdBits[0]));
        return -1;
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

    private int findIndexById(int id) {
        for (int i = 0; i < wiseSayingsSize; i++) {
            if (wiseSayings[i].getId() == id) return i;
        }

        return -1;
    }

    // 지운 칸 뒤의 명언들을 한 칸씩 앞으로 당긴다
    private boolean delete(int id) {
        int index = findIndexById(id);
        if (index == -1) return false;

        System.arraycopy(wiseSayings, index + 1, wiseSayings, index, wiseSayingsSize - index - 1);
        wiseSayings[--wiseSayingsSize] = null;

        return true;
    }
}
