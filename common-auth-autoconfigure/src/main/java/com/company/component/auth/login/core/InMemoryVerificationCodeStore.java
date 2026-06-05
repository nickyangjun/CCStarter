package com.company.component.auth.login.core;

import com.company.component.auth.login.spi.VerificationCodeStore;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 进程内验证码存储（仅 dev/sample）。
 */
public class InMemoryVerificationCodeStore implements VerificationCodeStore {

    private final Map<String, Entry> codes = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastSend = new ConcurrentHashMap<>();

    @Override
    public void save(String identity, String code, Duration ttl) {
        Instant expireAt = Instant.now().plus(ttl);
        codes.put(identity, new Entry(code, expireAt));
        lastSend.put(identity, Instant.now());
    }

    @Override
    public boolean verifyAndConsume(String identity, String code) {
        Entry entry = codes.get(identity);
        if (entry == null || Instant.now().isAfter(entry.expireAt)) {
            codes.remove(identity);
            return false;
        }
        if (!entry.code.equals(code)) {
            return false;
        }
        codes.remove(identity);
        return true;
    }

    @Override
    public Optional<Duration> timeSinceLastSend(String identity) {
        Instant at = lastSend.get(identity);
        if (at == null) {
            return Optional.empty();
        }
        return Optional.of(Duration.between(at, Instant.now()));
    }

    private record Entry(String code, Instant expireAt) {
    }
}
