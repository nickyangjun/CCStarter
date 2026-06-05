package com.company.component.sample;

import com.company.component.dict.core.DictException;
import com.company.component.dict.core.DictItem;
import com.company.component.dict.core.DictService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class DictIntegrationTest {

    @Autowired
    private DictService dictService;

    @Test
    void getGenderItems() {
        List<DictItem> items = dictService.getItems("gender");
        assertThat(items).hasSize(2);
        assertThat(items.get(0).label()).isEqualTo("男");
        assertThat(items.get(0).value()).isEqualTo("M");
    }

    @Test
    void getLabelAndValue() {
        assertThat(dictService.getLabel("gender", "2")).contains("女");
        assertThat(dictService.getValue("order_status", "FULL_PAID")).contains("PAID");
    }

    @Test
    void requireLabelThrowsForMissingCode() {
        assertThatThrownBy(() -> dictService.requireLabel("gender", "unknown"))
                .isInstanceOf(DictException.class)
                .hasFieldOrPropertyWithValue("errorCode", "DICT_ENTRY_NOT_FOUND");
    }

    @Test
    void refreshReloadsFromProvider() {
        dictService.getItems("gender");
        dictService.refresh("gender");
        assertThat(dictService.getItems("gender")).hasSize(2);
    }
}
