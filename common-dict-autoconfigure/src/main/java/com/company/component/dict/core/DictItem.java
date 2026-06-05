package com.company.component.dict.core;

import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 字典项领域模型（camelCase）；表列映射见设计文档 §4.0 / §5.1。
 */
public final class DictItem {

    private final String dictType;
    private final String code;
    private final String label;
    private final String value;
    private final int sortOrder;
    private final String cssClass;
    private final Map<String, Object> extra;

    private DictItem(String dictType,
                     String code,
                     String label,
                     String value,
                     int sortOrder,
                     String cssClass,
                     Map<String, Object> extra) {
        this.dictType = dictType;
        this.code = code;
        this.label = label;
        this.value = value;
        this.sortOrder = sortOrder;
        this.cssClass = cssClass;
        this.extra = extra;
    }

    public static DictItem of(String dictType,
                              String code,
                              String label,
                              String rawValue,
                              int sortOrder,
                              String cssClass,
                              Map<String, Object> extra) {
        String effectiveValue = StringUtils.hasText(rawValue) ? rawValue.trim() : code;
        Map<String, Object> safeExtra = extra == null || extra.isEmpty() ? Map.of() : Map.copyOf(extra);
        return new DictItem(dictType, code, label, effectiveValue, sortOrder, cssClass, safeExtra);
    }

    public String dictType() {
        return dictType;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public String value() {
        return value;
    }

    public int sortOrder() {
        return sortOrder;
    }

    public String cssClass() {
        return cssClass;
    }

    public Map<String, Object> extra() {
        return extra;
    }
}
