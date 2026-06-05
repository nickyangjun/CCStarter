package com.company.component.dict.support;

import com.company.component.dict.properties.DictProperties;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 收集字典 HTTP API 路径，供 auth 白名单合并。
 */
public final class DictPathCollector {

    private DictPathCollector() {
    }

    public static List<String> collect(DictProperties dict) {
        List<String> paths = new ArrayList<>();
        if (dict == null || !dict.isEnabled() || !dict.getApi().isEnabled()) {
            return paths;
        }
        String basePath = dict.getApi().getBasePath().trim();
        if (!StringUtils.hasText(basePath)) {
            return paths;
        }
        if (basePath.endsWith("/**")) {
            paths.add(basePath);
        } else {
            paths.add(basePath + "/**");
        }
        return paths;
    }
}
