package com.hotel.config;

import com.hotel.common.LoginRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final LoginRateLimiter rateLimiter;

    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredRateLimits() {
        rateLimiter.cleanup();
    }
}
