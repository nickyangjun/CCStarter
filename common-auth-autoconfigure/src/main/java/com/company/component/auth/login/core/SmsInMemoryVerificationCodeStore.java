package com.company.component.auth.login.core;

import com.company.component.auth.login.spi.SmsCodeStore;

/**
 * 短信通道专用内存存储（与邮箱存储隔离 Bean 类型）。
 */
public final class SmsInMemoryVerificationCodeStore extends InMemoryVerificationCodeStore implements SmsCodeStore {
}
