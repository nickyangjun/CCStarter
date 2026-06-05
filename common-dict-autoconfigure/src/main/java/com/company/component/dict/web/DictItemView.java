package com.company.component.dict.web;

import com.company.component.dict.core.DictItem;

import java.util.Map;

public record DictItemView(
        String code,
        String label,
        String value,
        int sortOrder,
        String cssClass,
        Map<String, Object> extra) {

    public static DictItemView from(DictItem item) {
        Map<String, Object> extra = item.extra().isEmpty() ? null : item.extra();
        return new DictItemView(
                item.code(),
                item.label(),
                item.value(),
                item.sortOrder(),
                item.cssClass(),
                extra);
    }
}
