package com.company.component.auth.login.spi;

import java.time.Duration;
import java.util.Optional;

/**
 * 验证码存储（短信/邮箱共用）。
 */
public interface VerificationCodeStore {

    void save(String identity, String code, Duration ttl);

    boolean verifyAndConsume(String identity, String code);

    Optional<Duration> timeSinceLastSend(String identity);
}
