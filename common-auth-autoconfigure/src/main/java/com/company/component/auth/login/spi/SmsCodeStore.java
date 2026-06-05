package com.company.component.auth.login.spi;

/**
 * 短信验证码存储（正式环境建议 Redis 实现）。
 */
public interface SmsCodeStore extends VerificationCodeStore {
}
