package com.wty.ratelimter.data;

import com.google.common.util.concurrent.RateLimiter;
import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class QpsLimitData {
    private String uri;
    private AtomicInteger queueCount;
    private QpsLimitCfg qpsLimitCfg;
    private RateLimiter rateLimiter;

    private long lastAccessTime = 0;

    public QpsLimitData(String uri, QpsLimitCfg qpsLimitCfg) {
        this.uri = uri;
        this.queueCount = new AtomicInteger(0);
        this.qpsLimitCfg = qpsLimitCfg;
        this.rateLimiter = RateLimiter.create(qpsLimitCfg.apiLimitQps(uri));
    }

    public boolean expire() {
        return lastAccessTime != 0 && (System.currentTimeMillis() - lastAccessTime > qpsLimitCfg.cleanTimeoutMs(uri));
    }
}
