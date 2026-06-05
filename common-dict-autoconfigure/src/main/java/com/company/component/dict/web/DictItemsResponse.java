package com.company.component.dict.web;

import java.util.List;

public record DictItemsResponse(String dictType, List<DictItemView> items) {
}
