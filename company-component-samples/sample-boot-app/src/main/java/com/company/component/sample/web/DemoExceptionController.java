package com.company.component.sample.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用于验证 common-exception 组件的演示接口（仅 sample 工程）。
 */
@RestController
@RequestMapping("/api/sample/error")
public class DemoExceptionController {

    @GetMapping("/runtime")
    public void runtime() {
        throw new RuntimeException("sample runtime error");
    }

    /**
     * 缺少 {@code name} 参数时触发 {@link org.springframework.web.bind.MissingServletRequestParameterException}。
     */
    @GetMapping("/missing-param")
    public void missingParam(@RequestParam String name) {
    }
}
