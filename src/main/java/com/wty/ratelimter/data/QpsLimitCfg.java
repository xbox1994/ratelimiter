package com.wty.ratelimter.data;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class QpsLimitCfg {
    private static final int DEFAULT_QPS_LIMIT = 100;
    private static final int DEFAULT_QUEUE_SIZE = 10;
    private static final int DEFAULT_QUEUE_ALL = 500;
    private static final int DEFAULT_CHECK_CNT = 10;
    private static final long DEFAULT_CHECK_WAIT_MS = 10;
    private static final long DEFAULT_EXPIRE_TIMEOUT = 600000L;

    private Boolean enable;
    private Set<String> printResponseUri; // 需要打印返回值的请求地址
    private List<String> includeUrls;
    private List<String> excludeUrls;
    private Map<String, ApiQpsLimitCfg> apiQpsLimitCfgMap; // 单个api限制
}
