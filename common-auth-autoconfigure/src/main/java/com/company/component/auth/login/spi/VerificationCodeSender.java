package com.company.component.auth.login.spi;

/**
 * 验证码发送（短信或邮件通道实现）。
 */
public interface VerificationCodeSender {

    void send(String identity, String code);
}
