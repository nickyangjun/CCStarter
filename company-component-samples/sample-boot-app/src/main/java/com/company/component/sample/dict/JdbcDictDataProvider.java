package com.company.component.sample.dict;

import com.company.component.dict.core.DictItem;
import com.company.component.dict.spi.DictDataProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 从 {@code sys_dict_type} / {@code sys_dict_item} 加载字典（sample 演示实现）。
 */
public final class JdbcDictDataProvider implements DictDataProvider {

    private static final String LOAD_SQL = """
            SELECT i.dict_type, i.item_code, i.item_label, i.item_value, i.sort_order, i.css_class, i.extra_json
            FROM sys_dict_item i
            INNER JOIN sys_dict_type t ON t.dict_type = i.dict_type
            WHERE t.dict_type = ? AND t.enabled = 1 AND i.enabled = 1
            ORDER BY i.sort_order ASC, i.id ASC
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcDictDataProvider(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<DictItem> loadByType(String dictType) {
        if (!StringUtils.hasText(dictType)) {
            throw new IllegalArgumentException("dictType must not be blank");
        }
        try {
            return jdbcTemplate.query(LOAD_SQL, (rs, rowNum) -> {
                String type = rs.getString("dict_type");
                String code = rs.getString("item_code");
                String label = rs.getString("item_label");
                String rawValue = rs.getString("item_value");
                int sortOrder = rs.getInt("sort_order");
                String cssClass = rs.getString("css_class");
                String extraJson = rs.getString("extra_json");
                Map<String, Object> extra = parseExtra(extraJson, type, code);
                return DictItem.of(type, code, label, rawValue, sortOrder, cssClass, extra);
            }, dictType.trim());
        } catch (DataAccessException ex) {
            throw new IllegalStateException("Failed to load dict items for type: " + dictType, ex);
        }
    }

    private Map<String, Object> parseExtra(String extraJson, String dictType, String code) {
        if (!StringUtils.hasText(extraJson)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(extraJson.trim(), new TypeReference<>() {
            });
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to parse extra_json for dictType=" + dictType + ", code=" + code, ex);
        }
    }
}
