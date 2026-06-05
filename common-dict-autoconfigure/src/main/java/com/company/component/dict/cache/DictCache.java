package com.company.component.dict.cache;

import com.company.component.dict.core.DictItem;

import java.util.List;
import java.util.Optional;

/**
 * 字典缓存抽象。
 * <ul>
 *   <li>{@link Optional#empty()} — 未命中</li>
 *   <li>{@link Optional#of(List)} — 命中（列表可为空）</li>
 * </ul>
 */
public interface DictCache {

    Optional<List<DictItem>> get(String dictType);

    void put(String dictType, List<DictItem> items);

    void evict(String dictType);

    void evictAll();
}
