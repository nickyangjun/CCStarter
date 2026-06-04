package com.company.component.auth.support;

import org.springframework.core.Ordered;

public final class OrderConstants {

    public static final int SECURITY_FILTER_CHAIN = Ordered.HIGHEST_PRECEDENCE + 100;

    private OrderConstants() {
    }
}
