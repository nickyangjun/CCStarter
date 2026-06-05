package com.company.component.auth.login.support;

import com.company.component.auth.login.core.LoginAuthException;
import com.company.component.auth.login.spi.VerificationCodeSender;
import com.company.component.auth.login.spi.VerificationCodeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;

/**
 * 短信/邮箱验证码共用发送与校验逻辑。
 */
public final class VerificationCodeEngine {

    private static final Logger log = LoggerFactory.getLogger(VerificationCodeEngine.class);
    private final SecureRandom random = new SecureRandom();

    public void sendCode(String identityRaw,
                         ChannelSettings settings,
                         VerificationCodeStore store,
                         VerificationCodeSender sender) {
        String identity = settings.normalize().apply(identityRaw);
        if (settings.testEnabled()) {
            log.debug("login test mode: skip store and send for identity={}", settings.mask().apply(identity));
            return;
        }
        Optional<Duration> since = store.timeSinceLastSend(identity);
        if (since.isPresent() && since.get().getSeconds() < settings.resendIntervalSeconds()) {
            throw settings.sendTooFrequent().get();
        }
        String code = generateCode(settings.codeLength());
        store.save(identity, code, Duration.ofSeconds(settings.codeTtlSeconds()));
        try {
            sender.send(identity, code);
        } catch (RuntimeException ex) {
            throw settings.sendFailed().apply(ex.getMessage());
        }
    }

    public void verifyCode(String identityRaw, String codeRaw, ChannelSettings settings, VerificationCodeStore store) {
        String identity = settings.normalize().apply(identityRaw);
        String code = requireCode(codeRaw, settings);
        if (settings.testEnabled()) {
            verifyTestMode(identity, code, settings);
            return;
        }
        if (!store.verifyAndConsume(identity, code)) {
            throw settings.invalidCode().get();
        }
    }

    private void verifyTestMode(String identity, String code, ChannelSettings settings) {
        if (StringUtils.hasText(settings.testFixedCode())) {
            if (!settings.testFixedCode().equals(code)) {
                throw settings.invalidCode().get();
            }
            return;
        }
        if (settings.testMobileSuffix()) {
            String expected = settings.testSuffixResolver().apply(identity);
            if (!expected.equals(code)) {
                throw settings.invalidCode().get();
            }
        }
    }

    private String generateCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private static String requireCode(String codeRaw, ChannelSettings settings) {
        if (!StringUtils.hasText(codeRaw)) {
            throw settings.invalidCode().get();
        }
        String code = codeRaw.trim();
        if (code.length() != settings.codeLength() || !code.chars().allMatch(Character::isDigit)) {
            throw settings.invalidCode().get();
        }
        return code;
    }

    public record ChannelSettings(
            int codeLength,
            int codeTtlSeconds,
            int resendIntervalSeconds,
            boolean testEnabled,
            String testFixedCode,
            boolean testMobileSuffix,
            Function<String, String> normalize,
            Function<String, String> mask,
            Function<String, String> testSuffixResolver,
            java.util.function.Supplier<LoginAuthException> invalidCode,
            java.util.function.Supplier<LoginAuthException> sendTooFrequent,
            Function<String, LoginAuthException> sendFailed) {
    }
}
