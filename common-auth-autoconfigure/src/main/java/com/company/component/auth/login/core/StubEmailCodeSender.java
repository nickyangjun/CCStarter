package com.company.component.auth.login.core;

import com.company.component.auth.login.spi.EmailCodeSender;
import com.company.component.auth.login.support.EmailSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 占位邮件发送器：不调用第三方邮件服务。
 */
public class StubEmailCodeSender implements EmailCodeSender {

    private static final Logger log = LoggerFactory.getLogger(StubEmailCodeSender.class);

    @Override
    public void send(String identity, String code) {
        log.warn("EmailCodeSender stub: email not sent (email={}, codeLength={}). "
                + "Provide an EmailCodeSender bean for production.", EmailSupport.mask(identity),
                code != null ? code.length() : 0);
    }
}
