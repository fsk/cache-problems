package com.lab.level1.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class Level1Logger {

    private final Logger delegate;

    public Level1Logger() {
        this.delegate = LoggerFactory.getLogger(Level1Logger.class);
    }

    public void info(String tag, String message, Object... kv) {
        delegate.info("{} {} {}", tag, message, format(kv));
    }

    private static String format(Object... kv) {
        if (kv == null || kv.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < kv.length; i += 2) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(kv[i]).append('=').append(i + 1 < kv.length ? kv[i + 1] : "?");
        }
        return sb.toString();
    }
}
