package com.company.component.sample.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/sample/ping")
    public Map<String, String> ping() {
        return Map.of("status", "ok", "app", "sample-boot-app");
    }
}
