package com.company.component.auth.login.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * 登录编排配置，前缀 {@code component.auth.login}。
 */
@ConfigurationProperties(prefix = "component.auth.login")
public class LoginProperties {

    private boolean enabled = false;

    private int smsLength = 6;

    private int emailCodeLength = 6;

    private final Sms sms = new Sms();

    private final Email email = new Email();

    private final Register register = new Register();

    private final Test test = new Test();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getSmsLength() {
        return smsLength;
    }

    public void setSmsLength(int smsLength) {
        this.smsLength = smsLength;
    }

    public int getEmailCodeLength() {
        return emailCodeLength;
    }

    public void setEmailCodeLength(int emailCodeLength) {
        this.emailCodeLength = emailCodeLength;
    }

    public Sms getSms() {
        return sms;
    }

    public Email getEmail() {
        return email;
    }

    public Register getRegister() {
        return register;
    }

    public Test getTest() {
        return test;
    }

    public boolean isAnyChannelEnabled() {
        return sms.isEnabled() || email.isEnabled();
    }

    public static class Sms {

        private boolean enabled = false;

        private int codeTtlSeconds = 300;

        private int resendIntervalSeconds = 60;

        private final Paths paths = new Paths();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getCodeTtlSeconds() {
            return codeTtlSeconds;
        }

        public void setCodeTtlSeconds(int codeTtlSeconds) {
            this.codeTtlSeconds = codeTtlSeconds;
        }

        public int getResendIntervalSeconds() {
            return resendIntervalSeconds;
        }

        public void setResendIntervalSeconds(int resendIntervalSeconds) {
            this.resendIntervalSeconds = resendIntervalSeconds;
        }

        public Paths getPaths() {
            return paths;
        }
    }

    public static class Email {

        private boolean enabled = false;

        private int codeTtlSeconds = 300;

        private int resendIntervalSeconds = 60;

        private final EmailPaths paths = new EmailPaths();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getCodeTtlSeconds() {
            return codeTtlSeconds;
        }

        public void setCodeTtlSeconds(int codeTtlSeconds) {
            this.codeTtlSeconds = codeTtlSeconds;
        }

        public int getResendIntervalSeconds() {
            return resendIntervalSeconds;
        }

        public void setResendIntervalSeconds(int resendIntervalSeconds) {
            this.resendIntervalSeconds = resendIntervalSeconds;
        }

        public EmailPaths getPaths() {
            return paths;
        }
    }

    public static class Paths {

        private String sendCode = "/api/auth/sms/send-code";

        private String login = "/api/auth/sms/login";

        private String register = "/api/auth/sms/register";

        public String getSendCode() {
            return sendCode;
        }

        public void setSendCode(String sendCode) {
            this.sendCode = sendCode;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getRegister() {
            return register;
        }

        public void setRegister(String register) {
            this.register = register;
        }
    }

    public static class EmailPaths {

        private String sendCode = "/api/auth/email/send-code";

        private String login = "/api/auth/email/login";

        private String register = "/api/auth/email/register";

        public String getSendCode() {
            return sendCode;
        }

        public void setSendCode(String sendCode) {
            this.sendCode = sendCode;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getRegister() {
            return register;
        }

        public void setRegister(String register) {
            this.register = register;
        }
    }

    public static class Register {

        private boolean enabled = false;

        private boolean loginAsRegister = false;

        private boolean issueTokenOnRegister = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isLoginAsRegister() {
            return loginAsRegister;
        }

        public void setLoginAsRegister(boolean loginAsRegister) {
            this.loginAsRegister = loginAsRegister;
        }

        public boolean isIssueTokenOnRegister() {
            return issueTokenOnRegister;
        }

        public void setIssueTokenOnRegister(boolean issueTokenOnRegister) {
            this.issueTokenOnRegister = issueTokenOnRegister;
        }
    }

    /**
     * 测试验码配置（短信 / 邮箱分通道）。
     * <p>
     * 开启 {@code test.sms.enabled} 或 {@code test.email.enabled} 后，登录/注册将走
     * <strong>固定验证码</strong>（或短信 {@code mobile-suffix}），不调用真实短信/邮件、不写正式验证码存储。
     * </p>
     * <p>
     * 根级 {@code enabled}/{@code fixed-code}/{@code mobile-suffix} 为兼容旧配置，行为等同 {@code test.sms}。
     * </p>
     */
    public static class Test {

        /** @deprecated 使用 {@link #sms}.{@link ChannelTest#enabled} */
        private boolean enabled = false;

        /** @deprecated 使用 {@link #sms}.{@link ChannelTest#fixedCode} */
        private String fixedCode;

        /** @deprecated 使用 {@link #sms}.{@link ChannelTest#mobileSuffix} */
        private boolean mobileSuffix = false;

        /**
         * 生产防呆确认开关，默认 {@code false}。
         * <p>
         * 当任意测试验码通道生效（{@link #isAnyTestActive()} 为 true）时，<strong>必须</strong>设为 {@code true}，
         * 否则 {@link com.company.component.auth.login.support.LoginPropertiesValidator} 会在启动阶段抛错并阻止应用启动。
         * </p>
         * <p>
         * <strong>含义</strong>：并非允许在生产环境使用测试码，而是声明「当前部署环境已知情并有意启用测试验码」，
         * 防止测试配置被误合并进生产配置中心却静默生效。
         * </p>
         * <ul>
         *   <li>本地开发 / sample / CI：在仅用于测试的 yml（如 {@code application-test.yml}）中可设为 {@code true}</li>
         *   <li>正式生产：不要开启 {@code test.sms} / {@code test.email}；本项保持默认 {@code false}</li>
         * </ul>
         */
        private boolean allowInProduction = false;

        private final ChannelTest sms = new ChannelTest();

        private final ChannelTest email = new ChannelTest();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getFixedCode() {
            return fixedCode;
        }

        public void setFixedCode(String fixedCode) {
            this.fixedCode = fixedCode;
        }

        public boolean isMobileSuffix() {
            return mobileSuffix;
        }

        public void setMobileSuffix(boolean mobileSuffix) {
            this.mobileSuffix = mobileSuffix;
        }

        public boolean isAllowInProduction() {
            return allowInProduction;
        }

        public void setAllowInProduction(boolean allowInProduction) {
            this.allowInProduction = allowInProduction;
        }

        public ChannelTest getSms() {
            return sms;
        }

        public ChannelTest getEmail() {
            return email;
        }

        public boolean isSmsTestActive() {
            if (sms.isEnabled()) {
                return true;
            }
            return enabled && (StringUtils.hasText(fixedCode) || mobileSuffix);
        }

        public boolean isEmailTestActive() {
            return email.isEnabled();
        }

        public boolean isAnyTestActive() {
            return isSmsTestActive() || isEmailTestActive();
        }

        public String effectiveSmsFixedCode() {
            if (StringUtils.hasText(sms.getFixedCode())) {
                return sms.getFixedCode();
            }
            return fixedCode;
        }

        public boolean effectiveSmsMobileSuffix() {
            if (sms.isEnabled() && StringUtils.hasText(sms.getFixedCode())) {
                return false;
            }
            if (sms.isMobileSuffix()) {
                return true;
            }
            return enabled && mobileSuffix && !StringUtils.hasText(effectiveSmsFixedCode());
        }

        public String effectiveEmailFixedCode() {
            return email.getFixedCode();
        }
    }

    public static class ChannelTest {

        private boolean enabled = false;

        private String fixedCode;

        private boolean mobileSuffix = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getFixedCode() {
            return fixedCode;
        }

        public void setFixedCode(String fixedCode) {
            this.fixedCode = fixedCode;
        }

        public boolean isMobileSuffix() {
            return mobileSuffix;
        }

        public void setMobileSuffix(boolean mobileSuffix) {
            this.mobileSuffix = mobileSuffix;
        }
    }
}
