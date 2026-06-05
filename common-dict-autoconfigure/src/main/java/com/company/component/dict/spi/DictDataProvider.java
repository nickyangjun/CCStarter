package com.company.component.dict.spi;

import com.company.component.dict.core.DictItem;

import java.util.List;

/**
 * 字典数据源 SPI；业务从 {@code sys_dict_type} / {@code sys_dict_item} 加载。
 */
public interface DictDataProvider {

    /**
     * 加载指定类型的有效字典项（已过滤 disabled、已排序）。
     *
     * @throws RuntimeException 数据源不可用时抛出，禁止吞异常
     */
    List<DictItem> loadByType(String dictType);
}
