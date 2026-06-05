package com.company.component.auth.login.core;

import com.company.component.exception.spi.MappedHttpStatusException;

/**
 * 登录编排领域异常，由 {@link com.company.component.auth.login.support.LoginExceptionErrorCodeResolver} 映射为统一 JSON。
 */
public class LoginAuthException extends RuntimeException implements MappedHttpStatusException {

    private final String errorCode;
    private final int httpStatus;

    public LoginAuthException(String errorCode, int httpStatus, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    public static LoginAuthException invalidMobile() {
        return new LoginAuthException("INVALID_MOBILE", 400, "手机号格式不正确");
    }

    public static LoginAuthException invalidSmsCode() {
        return new LoginAuthException("INVALID_SMS_CODE", 400, "验证码错误或已失效");
    }

    public static LoginAuthException sendTooFrequent() {
        return new LoginAuthException("SMS_SEND_TOO_FREQUENT", 429, "发送过于频繁，请稍后再试");
    }

    public static LoginAuthException sendFailed(String detail) {
        return new LoginAuthException("SMS_SEND_FAILED", 502, detail != null ? detail : "短信发送失败");
    }

    public static LoginAuthException userNotFound() {
        return new LoginAuthException("USER_NOT_FOUND", 404, "用户不存在");
    }

    public static LoginAuthException mobileAlreadyRegistered() {
        return new LoginAuthException("MOBILE_ALREADY_REGISTERED", 409, "手机号已注册");
    }

    public static LoginAuthException invalidEmail() {
        return new LoginAuthException("INVALID_EMAIL", 400, "邮箱格式不正确");
    }

    public static LoginAuthException invalidEmailCode() {
        return new LoginAuthException("INVALID_EMAIL_CODE", 400, "邮箱验证码错误或已失效");
    }

    public static LoginAuthException emailSendTooFrequent() {
        return new LoginAuthException("EMAIL_SEND_TOO_FREQUENT", 429, "邮件发送过于频繁，请稍后再试");
    }

    public static LoginAuthException emailSendFailed(String detail) {
        return new LoginAuthException("EMAIL_SEND_FAILED", 502, detail != null ? detail : "邮件发送失败");
    }

    public static LoginAuthException emailAlreadyRegistered() {
        return new LoginAuthException("EMAIL_ALREADY_REGISTERED", 409, "邮箱已注册");
    }
}
