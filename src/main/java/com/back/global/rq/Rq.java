package com.back.global.rq;

import java.util.HashMap;
import java.util.Map;

// "삭제?id=1" 같은 명령 문자열을 명령 이름과 파라미터로 나눠 준다
public class Rq {
    private final String actionName;
    private final Map<String, String> params = new HashMap<>();

    public Rq(String cmd) {
        String[] cmdBits = cmd.trim().split("[?]", 2);

        actionName = cmdBits[0].trim();

        if (cmdBits.length < 2) return;

        for (String param : cmdBits[1].split("&")) {
            String[] keyValue = param.split("=", 2);

            if (keyValue.length < 2) continue;

            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            if (key.isEmpty() || value.isEmpty()) continue;

            params.put(key, value);
        }
    }

    public String getActionName() {
        return actionName;
    }

    public String getParam(String name, String defaultValue) {
        return params.getOrDefault(name, defaultValue);
    }

    public int getParamAsInt(String name, int defaultValue) {
        try {
            return Integer.parseInt(getParam(name, ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
