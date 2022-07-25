package com.wty.ratelimter.utils;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;

import java.util.List;

public class PathMatcherUtils {
    public static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    public static boolean pathMatch(String uri, List<String> pathList) {
        if (CollectionUtils.isEmpty(pathList)) {
            return false;
        }
        for (String path : pathList) {
            if (PATH_MATCHER.match(path, uri)) {
                return true;
            }
        }
        return false;
    }
}
