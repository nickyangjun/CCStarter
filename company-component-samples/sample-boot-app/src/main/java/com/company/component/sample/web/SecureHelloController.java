package com.company.component.sample.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/sample/secure")
public class SecureHelloController {

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "secure-ok");
    }
}
