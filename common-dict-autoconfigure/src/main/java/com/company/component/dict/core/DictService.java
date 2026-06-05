package com.company.component.dict.core;

import com.company.component.dict.cache.DictCache;
import com.company.component.dict.properties.DictProperties;
import com.company.component.dict.spi.DictDataProvider;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

public class DictService {

    private final DictProperties properties;
    private final DictCache cache;
    private final DictDataProvider dataProvider;

    public DictService(DictProperties properties, DictCache cache, DictDataProvider dataProvider) {
        this.properties = properties;
        this.cache = cache;
        this.dataProvider = dataProvider;
    }

    public List<DictItem> getItems(String dictType) {
        String type = requireDictType(dictType);
        Optional<List<DictItem>> cached = cache.get(type);
        if (cached.isPresent()) {
            return List.copyOf(cached.get());
        }
        try {
            List<DictItem> loaded = dataProvider.loadByType(type);
            List<DictItem> snapshot = List.copyOf(loaded);
            cache.put(type, snapshot);
            return snapshot;
        } catch (RuntimeException ex) {
            throw DictException.loadFailed(type, ex.getMessage());
        }
    }

    public Optional<String> getLabel(String dictType, String code) {
        requireCode(code);
        return findItem(dictType, code).map(DictItem::label);
    }

    public Optional<String> getValue(String dictType, String code) {
        requireCode(code);
        return findItem(dictType, code).map(DictItem::value);
    }

    public String requireLabel(String dictType, String code) {
        return getLabel(dictType, code).orElseThrow(() -> DictException.entryNotFound(requireDictType(dictType), code.trim()));
    }

    public String requireValue(String dictType, String code) {
        return getValue(dictType, code).orElseThrow(() -> DictException.entryNotFound(requireDictType(dictType), code.trim()));
    }

    public void refresh(String dictType) {
        cache.evict(requireDictType(dictType));
    }

    public void refreshAll() {
        cache.evictAll();
    }

    public DictProperties properties() {
        return properties;
    }

    private Optional<DictItem> findItem(String dictType, String code) {
        String normalizedCode = code.trim();
        return getItems(dictType).stream()
                .filter(item -> item.code().equals(normalizedCode))
                .findFirst();
    }

    private static String requireDictType(String dictType) {
        if (!StringUtils.hasText(dictType)) {
            throw DictException.invalidDictType(dictType);
        }
        return dictType.trim();
    }

    private static void requireCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw DictException.entryNotFound("", code);
        }
    }
}
