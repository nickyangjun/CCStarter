package com.company.component.sample.dict;

import com.company.component.dict.spi.DictDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * sample 字典数据源：Flyway 建表 + JDBC 加载（替代内存模拟）。
 */
@Configuration
public class SampleDictJdbcConfiguration {

    @Bean
    DictDataProvider sampleDictDataProvider(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        return new JdbcDictDataProvider(jdbcTemplate, objectMapper);
    }
}
