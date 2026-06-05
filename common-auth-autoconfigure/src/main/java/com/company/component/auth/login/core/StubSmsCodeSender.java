package com.company.component.auth.login.core;

import com.company.component.auth.login.spi.SmsCodeSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 占位发送器：不调用第三方 SDK。正式环境请实现 {@link SmsCodeSender} 并注册为 Bean。
 */
public class StubSmsCodeSender implements SmsCodeSender {

    private static final Logger log = LoggerFactory.getLogger(StubSmsCodeSender.class);

    @Override
    public void send(String mobile, String code) {
        log.warn("SmsCodeSender stub: SMS not sent (mobile={}, codeLength={}). "
                + "Provide a SmsCodeSender bean for production.", mask(mobile), code != null ? code.length() : 0);
    }

    private static String mask(String mobile) {
        if (mobile == null || mobile.length() < 7) {
            return "***";
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }
}
