package com.company.component.auth.login.autoconfigure;

import com.company.component.auth.core.JwtService;
import com.company.component.auth.login.core.EmailAuthService;
import com.company.component.auth.login.core.EmailCodeService;
import com.company.component.auth.login.core.EmailInMemoryVerificationCodeStore;
import com.company.component.auth.login.core.SmsInMemoryVerificationCodeStore;
import com.company.component.auth.login.core.LoginTokenIssuer;
import com.company.component.auth.login.core.SmsAuthService;
import com.company.component.auth.login.core.SmsCodeService;
import com.company.component.auth.login.core.StubEmailCodeSender;
import com.company.component.auth.login.core.StubSmsCodeSender;
import com.company.component.auth.login.properties.LoginProperties;
import com.company.component.auth.login.spi.EmailCodeSender;
import com.company.component.auth.login.spi.EmailCodeStore;
import com.company.component.auth.login.spi.LoginUserRegistrar;
import com.company.component.auth.login.spi.LoginUserResolver;
import com.company.component.auth.login.spi.SmsCodeSender;
import com.company.component.auth.login.spi.SmsCodeStore;
import com.company.component.auth.login.support.LoginExceptionErrorCodeResolver;
import com.company.component.auth.login.support.LoginPropertiesValidator;
import com.company.component.auth.login.web.EmailLoginController;
import com.company.component.auth.login.web.EmailRegisterController;
import com.company.component.auth.login.web.SmsLoginController;
import com.company.component.auth.login.web.SmsRegisterController;
import com.company.component.auth.properties.AuthProperties;
import com.company.component.exception.spi.ExceptionErrorCodeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

@AutoConfiguration(after = com.company.component.auth.autoconfigure.AuthAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "component.auth.login", name = "enabled", havingValue = "true", matchIfMissing = false)
@ConditionalOnBean(JwtService.class)
public class LoginAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LoginAutoConfiguration.class);

    @Bean
    public Object componentLoginStartupGuard(LoginProperties login,
                                             AuthProperties auth,
                                             ObjectProvider<LoginUserResolver> resolverProvider,
                                             ObjectProvider<LoginUserRegistrar> registrarProvider) {
        LoginPropertiesValidator.validate(login, auth.isEnabled());
        if (login.getTest().isAnyTestActive() && !login.getTest().isAllowInProduction()) {
            log.warn("component.auth.login.test is active; ensure this is not enabled in production");
        }
        if (resolverProvider.getIfAvailable() == null) {
            throw new IllegalStateException("LoginUserResolver bean is required when component.auth.login.enabled=true");
        }
        boolean needRegistrar = login.getRegister().isEnabled() || login.getRegister().isLoginAsRegister();
        if (needRegistrar && registrarProvider.getIfAvailable() == null) {
            throw new IllegalStateException(
                    "LoginUserRegistrar bean is required when register.enabled or login-as-register is true");
        }
        return new Object();
    }

    @Bean(name = "componentLoginExceptionErrorCodeResolver")
    @ConditionalOnMissingBean(name = "componentLoginExceptionErrorCodeResolver")
    public ExceptionErrorCodeResolver componentLoginExceptionErrorCodeResolver() {
        return new LoginExceptionErrorCodeResolver();
    }

    @Bean(name = "componentSmsCodeStore")
    @ConditionalOnMissingBean(SmsCodeStore.class)
    @ConditionalOnProperty(prefix = "component.auth.login.sms", name = "enabled", havingValue = "true")
    public SmsCodeStore componentSmsCodeStore() {
        log.warn("Using in-memory SmsCodeStore; provide a SmsCodeStore bean for production.");
        return new SmsInMemoryVerificationCodeStore();
    }

    @Bean(name = "componentEmailCodeStore")
    @ConditionalOnMissingBean(EmailCodeStore.class)
    @ConditionalOnProperty(prefix = "component.auth.login.email", name = "enabled", havingValue = "true")
    public EmailCodeStore componentEmailCodeStore() {
        log.warn("Using in-memory EmailCodeStore; provide an EmailCodeStore bean for production.");
        return new EmailInMemoryVerificationCodeStore();
    }

    @Bean(name = "componentSmsCodeSender")
    @ConditionalOnMissingBean(SmsCodeSender.class)
    @ConditionalOnProperty(prefix = "component.auth.login.sms", name = "enabled", havingValue = "true")
    public SmsCodeSender componentSmsCodeSender() {
        log.warn("Using StubSmsCodeSender; provide SmsCodeSender for production SMS SDK (see 组件库实施进度 TODO).");
        return new StubSmsCodeSender();
    }

    @Bean(name = "componentEmailCodeSender")
    @ConditionalOnMissingBean(EmailCodeSender.class)
    @ConditionalOnProperty(prefix = "component.auth.login.email", name = "enabled", havingValue = "true")
    public EmailCodeSender componentEmailCodeSender() {
        log.warn("Using StubEmailCodeSender; provide EmailCodeSender for production mail service (see TODO).");
        return new StubEmailCodeSender();
    }

    @Bean
    @ConditionalOnProperty(prefix = "component.auth.login.sms", name = "enabled", havingValue = "true")
    @ConditionalOnBean(SmsCodeStore.class)
    public SmsCodeService componentSmsCodeService(LoginProperties login,
                                                  SmsCodeStore store,
                                                  SmsCodeSender sender) {
        return new SmsCodeService(login, store, sender);
    }

    @Bean
    @ConditionalOnProperty(prefix = "component.auth.login.email", name = "enabled", havingValue = "true")
    @ConditionalOnBean(EmailCodeStore.class)
    public EmailCodeService componentEmailCodeService(LoginProperties login,
                                                      EmailCodeStore store,
                                                      EmailCodeSender sender) {
        return new EmailCodeService(login, store, sender);
    }

    @Bean
    public LoginTokenIssuer componentLoginTokenIssuer(JwtService jwtService, AuthProperties authProperties) {
        return new LoginTokenIssuer(jwtService, authProperties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "component.auth.login.sms", name = "enabled", havingValue = "true")
    @ConditionalOnBean({SmsCodeService.class, LoginUserResolver.class})
    public SmsAuthService componentSmsAuthService(LoginProperties login,
                                                  SmsCodeService smsCodeService,
                                                  LoginTokenIssuer tokenIssuer,
                                                  LoginUserResolver userResolver,
                                                  ObjectProvider<LoginUserRegistrar> registrarProvider) {
        return new SmsAuthService(login, smsCodeService, tokenIssuer, userResolver,
                Optional.ofNullable(registrarProvider.getIfAvailable()));
    }

    @Bean
    @ConditionalOnProperty(prefix = "component.auth.login.email", name = "enabled", havingValue = "true")
    @ConditionalOnBean({EmailCodeService.class, LoginUserResolver.class})
    public EmailAuthService componentEmailAuthService(LoginProperties login,
                                                    EmailCodeService emailCodeService,
                                                    LoginTokenIssuer tokenIssuer,
                                                    LoginUserResolver userResolver,
                                                    ObjectProvider<LoginUserRegistrar> registrarProvider) {
        return new EmailAuthService(login, emailCodeService, tokenIssuer, userResolver,
                Optional.ofNullable(registrarProvider.getIfAvailable()));
    }

    @Bean
    @ConditionalOnBean(SmsAuthService.class)
    public SmsLoginController componentSmsLoginController(SmsCodeService smsCodeService,
                                                          SmsAuthService smsAuthService) {
        return new SmsLoginController(smsCodeService, smsAuthService);
    }

    @Bean
    @ConditionalOnProperty(prefix = "component.auth.login.register", name = "enabled", havingValue = "true")
    @ConditionalOnBean(SmsAuthService.class)
    public SmsRegisterController componentSmsRegisterController(SmsAuthService smsAuthService) {
        return new SmsRegisterController(smsAuthService);
    }

    @Bean
    @ConditionalOnBean(EmailAuthService.class)
    public EmailLoginController componentEmailLoginController(EmailCodeService emailCodeService,
                                                              EmailAuthService emailAuthService) {
        return new EmailLoginController(emailCodeService, emailAuthService);
    }

    @Bean
    @ConditionalOnProperty(prefix = "component.auth.login.register", name = "enabled", havingValue = "true")
    @ConditionalOnBean(EmailAuthService.class)
    public EmailRegisterController componentEmailRegisterController(EmailAuthService emailAuthService) {
        return new EmailRegisterController(emailAuthService);
    }
}
