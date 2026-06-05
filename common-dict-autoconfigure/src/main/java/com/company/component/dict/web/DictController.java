package com.company.component.dict.web;

import com.company.component.dict.core.DictItem;
import com.company.component.dict.core.DictService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DictController {

    private final DictService dictService;

    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    @GetMapping("${component.dict.api.base-path:/api/dict}/{dictType}")
    public ResponseEntity<DictItemsResponse> getItems(@PathVariable String dictType) {
        List<DictItem> items = dictService.getItems(dictType);
        List<DictItemView> views = items.stream().map(DictItemView::from).toList();
        return ResponseEntity.ok(new DictItemsResponse(dictType.trim(), views));
    }
}
