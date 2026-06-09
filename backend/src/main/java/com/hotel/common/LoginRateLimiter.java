package com.hotel.common;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    private final Map<String, AttemptRecord> records = new ConcurrentHashMap<>();

    public void recordFailure(String key) {
        records.compute(key, (k, v) -> {
            if (v == null) {
                return new AttemptRecord(1, null);
            }
            v.failCount++;
            if (v.failCount >= MAX_ATTEMPTS) {
                v.lockedUntil = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
            }
            return v;
        });
    }

    public void clear(String key) {
        records.remove(key);
    }

    public RateLimitResult check(String key) {
        AttemptRecord record = records.get(key);
        if (record == null) {
            return RateLimitResult.allow();
        }
        if (record.lockedUntil != null) {
            if (LocalDateTime.now().isBefore(record.lockedUntil)) {
                long remainingSeconds = java.time.Duration.between(LocalDateTime.now(), record.lockedUntil).getSeconds();
                return RateLimitResult.locked(remainingSeconds);
            }
            records.remove(key);
            return RateLimitResult.allow();
        }
        return RateLimitResult.allow();
    }

    public int getRemainingAttempts(String key) {
        AttemptRecord record = records.get(key);
        if (record == null) return MAX_ATTEMPTS;
        return Math.max(0, MAX_ATTEMPTS - record.failCount);
    }

    public void cleanup() {
        records.entrySet().removeIf(e -> {
            AttemptRecord v = e.getValue();
            return v.lockedUntil != null && LocalDateTime.now().isAfter(v.lockedUntil);
        });
    }

    private static class AttemptRecord {
        int failCount;
        LocalDateTime lockedUntil;

        AttemptRecord(int failCount, LocalDateTime lockedUntil) {
            this.failCount = failCount;
            this.lockedUntil = lockedUntil;
        }
    }

    public record RateLimitResult(boolean allowed, long remainingSeconds, String message) {
        public static RateLimitResult allow() {
            return new RateLimitResult(true, 0, null);
        }

        public static RateLimitResult locked(long remainingSeconds) {
            long minutes = remainingSeconds / 60;
            long seconds = remainingSeconds % 60;
            return new RateLimitResult(false, remainingSeconds,
                String.format("登录过于频繁，请在 %d分%02d秒 后重试", minutes, seconds));
        }
    }
}
