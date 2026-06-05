package com.company.component.dict.autoconfigure;

import com.company.component.dict.core.DictService;
import com.company.component.dict.web.DictController;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = DictAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "component.dict", name = "enabled", havingValue = "true")
@ConditionalOnBean(DictService.class)
public class DictWebAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "component.dict.api", name = "enabled", havingValue = "true")
    public DictController componentDictController(DictService dictService) {
        return new DictController(dictService);
    }
}
