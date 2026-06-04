package com.company.component.exception.support;

import org.springframework.core.Ordered;

/**
 * 组件内排序常量，见 docs/features/exception/design.md。
 */
public final class OrderConstants {

    /**
     * 全局异常 Advice 顺序（低于业务自定义更高优先级 Order 的值仍可覆盖）。
     */
    public static final int EXCEPTION_ADVICE = Ordered.LOWEST_PRECEDENCE - 100;

    private OrderConstants() {
    }
}
