package com.company.component.auth.login.core;

import com.company.component.auth.login.spi.EmailCodeStore;

/**
 * 邮箱通道专用内存存储（与短信存储隔离 Bean 类型）。
 */
public final class EmailInMemoryVerificationCodeStore extends InMemoryVerificationCodeStore implements EmailCodeStore {
}
