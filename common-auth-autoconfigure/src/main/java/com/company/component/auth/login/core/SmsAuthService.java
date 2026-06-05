package com.company.component.auth.login.core;

import com.company.component.auth.login.properties.LoginProperties;
import com.company.component.auth.login.spi.LoginUserRegistrar;
import com.company.component.auth.login.spi.LoginUserResolver;

import java.util.Map;
import java.util.Optional;

public class SmsAuthService {

    private final LoginProperties loginProperties;
    private final SmsCodeService smsCodeService;
    private final LoginTokenIssuer tokenIssuer;
    private final LoginUserResolver userResolver;
    private final Optional<LoginUserRegistrar> userRegistrar;

    public SmsAuthService(LoginProperties loginProperties,
                          SmsCodeService smsCodeService,
                          LoginTokenIssuer tokenIssuer,
                          LoginUserResolver userResolver,
                          Optional<LoginUserRegistrar> userRegistrar) {
        this.loginProperties = loginProperties;
        this.smsCodeService = smsCodeService;
        this.tokenIssuer = tokenIssuer;
        this.userResolver = userResolver;
        this.userRegistrar = userRegistrar;
    }

    public Map<String, Object> login(String mobile, String code) {
        smsCodeService.verifyCode(mobile, code);
        LoginPrincipal principal = resolveOrRegister(mobile, true);
        return tokenIssuer.issueTokenResponse(principal);
    }

    public Map<String, Object> register(String mobile, String code) {
        smsCodeService.verifyCode(mobile, code);
        if (userResolver.findByMobile(mobile).isPresent()) {
            throw LoginAuthException.mobileAlreadyRegistered();
        }
        LoginUserRegistrar registrar = userRegistrar.orElseThrow(() ->
                new IllegalStateException("LoginUserRegistrar bean is required when register is enabled"));
        LoginPrincipal created = registrar.register(RegisterRequest.forMobile(mobile));
        if (!loginProperties.getRegister().isIssueTokenOnRegister()) {
            return Map.of(
                    "userId", created.userId(),
                    "username", created.username());
        }
        return tokenIssuer.issueTokenResponse(created);
    }

    private LoginPrincipal resolveOrRegister(String mobile, boolean allowAutoRegister) {
        Optional<LoginPrincipal> existing = userResolver.findByMobile(mobile);
        if (existing.isPresent()) {
            return existing.get();
        }
        if (allowAutoRegister && loginProperties.getRegister().isLoginAsRegister()) {
            LoginUserRegistrar registrar = userRegistrar.orElseThrow(() ->
                    new IllegalStateException(
                            "LoginUserRegistrar bean is required when login-as-register=true"));
            return registrar.register(RegisterRequest.forMobile(mobile));
        }
        throw LoginAuthException.userNotFound();
    }
}
