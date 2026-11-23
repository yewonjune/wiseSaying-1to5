package com.back;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 실제 실행에서는 명언이 0개면 샘플 10개로 시작한다
        new App(scanner, true).run();

        scanner.close();
    }
}
