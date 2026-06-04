package com.company.component.log.support;

/**
 * MDC 键名，与 {@code docs/architecture/logging.md} 一致（SkyWalking 使用 {@code tid}）。
 */
public final class MdcKeys {

    public static final String TID = "tid";

    public static final String USER_ID = "userId";

    public static final String USERNAME = "username";

    private MdcKeys() {
    }
}
