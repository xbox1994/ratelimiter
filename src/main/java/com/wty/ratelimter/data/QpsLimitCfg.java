package com.wty.ratelimter.data;

import lombok.Data;

import java.util.*;

@Data
public class QpsLimitCfg {
    private static final int DEFAULT_QPS_LIMIT = 100;
    private static final int DEFAULT_QUEUE_SIZE = 10;
    private static final int DEFAULT_QUEUE_ALL = 500;
    private static final int DEFAULT_CHECK_CNT = 10;
    private static final long DEFAULT_CHECK_WAIT_MS = 10;
    private static final long DEFAULT_CLEAN_TIMEOUT_MS = 60000;
    private static final long DEFAULT_EXPIRE_TIMEOUT = 600000L;

    private Boolean enable;
    private Set<String> printResponseUri; // 需要打印返回值的请求地址
    private List<String> includeUrls;
    private List<String> excludeUrls;
    private Map<String, ApiQpsLimitCfg> apiQpsLimitCfgMap; // 单个api限制

    public QpsLimitCfg() {
        // TODO: 改为从配置中心获取
        this.enable = true;
        this.printResponseUri = new HashSet<>();
        this.includeUrls = List.of("/api/**");
        this.excludeUrls = Arrays.asList("/", "/**/*.css", "/**/*.html");
        Map<String, ApiQpsLimitCfg> apiQpsLimitCfgMap = new HashMap<>();
        ApiQpsLimitCfg apiQpsLimitCfg = new ApiQpsLimitCfg();
        apiQpsLimitCfg.setQps(1);
        apiQpsLimitCfg.setCheckWaitMs(100L);
        apiQpsLimitCfg.setCleanTimeoutMs(3000L);
        apiQpsLimitCfgMap.put("/api/qps1", apiQpsLimitCfg);
        this.apiQpsLimitCfgMap = apiQpsLimitCfgMap;
    }

    public ApiQpsLimitCfg findApiQpsLimitCfg(String uri) {
        ApiQpsLimitCfg cfg = new ApiQpsLimitCfg();
        cfg.setQueueAll(DEFAULT_QUEUE_ALL);
        cfg.setQueue(DEFAULT_QUEUE_SIZE);
        cfg.setQps(DEFAULT_QPS_LIMIT);
        cfg.setCheckCnt(DEFAULT_CHECK_CNT);
        cfg.setCheckWaitMs(DEFAULT_CHECK_WAIT_MS);
        cfg.setCleanTimeoutMs(DEFAULT_CLEAN_TIMEOUT_MS);
        ApiQpsLimitCfg apiQpsLimitCfg = apiQpsLimitCfgMap.get(uri);
        if (apiQpsLimitCfg != null) {
            if (apiQpsLimitCfg.getQueue() != null) {
                cfg.setQueue(apiQpsLimitCfg.getQueue());
            }
            if (apiQpsLimitCfg.getQps() != null) {
                cfg.setQps(apiQpsLimitCfg.getQps());
            }
            if (apiQpsLimitCfg.getCheckCnt() != null) {
                cfg.setCheckCnt(apiQpsLimitCfg.getCheckCnt());
            }
            if (apiQpsLimitCfg.getCheckWaitMs() != null) {
                cfg.setCheckWaitMs(apiQpsLimitCfg.getCheckWaitMs());
            }
            if (apiQpsLimitCfg.getCleanTimeoutMs() != null) {
                cfg.setCleanTimeoutMs(apiQpsLimitCfg.getCleanTimeoutMs());
            }
        }
        return cfg;
    }

    public int apiLimitQps(String uri) {
        ApiQpsLimitCfg apiQpsLimitCfg = findApiQpsLimitCfg(uri);
        return apiQpsLimitCfg.getQps();
    }

    public long cleanTimeoutMs(String uri) {
        ApiQpsLimitCfg apiQpsLimitCfg = findApiQpsLimitCfg(uri);
        return apiQpsLimitCfg.getCleanTimeoutMs();
    }
}
