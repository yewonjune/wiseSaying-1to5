package com.back.standard.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// JSON 라이브러리 없이, 숫자·문자열 값만 가진 평평한 객체를 만들고 읽는다
public final class Json {
    private static final String INDENT = "  ";

    // "key": "문자열" 또는 "key": 숫자
    private static final Pattern FIELD = Pattern.compile(
            "\"((?:\\\\.|[^\"\\\\])*)\"\\s*:\\s*(?:\"((?:\\\\.|[^\"\\\\])*)\"|(-?\\d+))"
    );

    private Json() {
    }

    // keys 순서대로 필드를 쓴다
    public static String toObject(Map<String, Object> map, List<String> keys) {
        String fields = keys.stream()
                .map(key -> INDENT + quote(key) + ": " + value(map.get(key)))
                .collect(Collectors.joining(",\n"));

        return "{\n" + fields + "\n}";
    }

    // 객체 JSON 문자열들을 한 단계 들여 써서 배열로 묶는다
    public static String toArray(List<String> objects) {
        if (objects.isEmpty()) return "[]";

        String items = objects.stream()
                .map(object -> object.lines()
                        .map(line -> INDENT + line)
                        .collect(Collectors.joining("\n")))
                .collect(Collectors.joining(",\n"));

        return "[\n" + items + "\n]";
    }

    public static Map<String, Object> parseObject(String json) {
        Map<String, Object> map = new LinkedHashMap<>();
        Matcher matcher = FIELD.matcher(json);

        while (matcher.find()) {
            String key = unescape(matcher.group(1));

            if (matcher.group(2) != null) {
                map.put(key, unescape(matcher.group(2)));
            } else {
                map.put(key, Long.parseLong(matcher.group(3)));
            }
        }

        return map;
    }

    private static String value(Object value) {
        if (value instanceof Number) return value.toString();
        return quote(String.valueOf(value));
    }

    private static String quote(String str) {
        return "\"" + str.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String unescape(String str) {
        return str.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
