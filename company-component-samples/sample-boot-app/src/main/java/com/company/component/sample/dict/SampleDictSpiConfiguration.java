package com.company.component.sample.dict;

import com.company.component.dict.core.DictItem;
import com.company.component.dict.spi.DictDataProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 演示 {@link DictDataProvider}：内存模拟 sys_dict_* 表数据（非真实 DB）。
 */
@Configuration
public class SampleDictSpiConfiguration {

    @Bean
    SampleDictStore sampleDictStore() {
        SampleDictStore store = new SampleDictStore();
        store.seed();
        return store;
    }

    @Bean
    DictDataProvider sampleDictDataProvider(SampleDictStore store) {
        return store::loadByType;
    }

    static final class SampleDictStore {

        private final Map<String, List<DictItem>> byType = new ConcurrentHashMap<>();

        void seed() {
            byType.put("gender", List.of(
                    DictItem.of("gender", "1", "男", "M", 1, null, null),
                    DictItem.of("gender", "2", "女", "F", 2, null, null)));
            byType.put("order_status", List.of(
                    DictItem.of("order_status", "CREATED", "已创建", "CREATED", 1, null, null),
                    DictItem.of("order_status", "FULL_PAID", "已支付", "PAID", 2, null, null)));
        }

        List<DictItem> loadByType(String dictType) {
            return byType.getOrDefault(dictType, List.of());
        }
    }
}
