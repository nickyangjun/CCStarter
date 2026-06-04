package com.company.component.sample.web;

import com.company.component.log.operation.OperationLog;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/sample/orders")
public class DemoOrderController {

    @OperationLog(module = "order", action = "create")
    @PostMapping
    public Map<String, String> create() {
        return Map.of("status", "created");
    }
}
