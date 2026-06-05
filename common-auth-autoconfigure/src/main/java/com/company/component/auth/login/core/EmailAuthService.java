package com.company.component.auth.login.core;

import com.company.component.auth.login.properties.LoginProperties;
import com.company.component.auth.login.spi.LoginUserRegistrar;
import com.company.component.auth.login.spi.LoginUserResolver;

import java.util.Map;
import java.util.Optional;

public class EmailAuthService {

    private final LoginProperties loginProperties;
    private final EmailCodeService emailCodeService;
    private final LoginTokenIssuer tokenIssuer;
    private final LoginUserResolver userResolver;
    private final Optional<LoginUserRegistrar> userRegistrar;

    public EmailAuthService(LoginProperties loginProperties,
                            EmailCodeService emailCodeService,
                            LoginTokenIssuer tokenIssuer,
                            LoginUserResolver userResolver,
                            Optional<LoginUserRegistrar> userRegistrar) {
        this.loginProperties = loginProperties;
        this.emailCodeService = emailCodeService;
        this.tokenIssuer = tokenIssuer;
        this.userResolver = userResolver;
        this.userRegistrar = userRegistrar;
    }

    public Map<String, Object> login(String email, String code) {
        emailCodeService.verifyCode(email, code);
        LoginPrincipal principal = resolveOrRegister(email, true);
        return tokenIssuer.issueTokenResponse(principal);
    }

    public Map<String, Object> register(String email, String code) {
        emailCodeService.verifyCode(email, code);
        if (userResolver.findByEmail(email).isPresent()) {
            throw LoginAuthException.emailAlreadyRegistered();
        }
        LoginUserRegistrar registrar = userRegistrar.orElseThrow(() ->
                new IllegalStateException("LoginUserRegistrar bean is required when register is enabled"));
        LoginPrincipal created = registrar.register(RegisterRequest.forEmail(email));
        if (!loginProperties.getRegister().isIssueTokenOnRegister()) {
            return Map.of(
                    "userId", created.userId(),
                    "username", created.username());
        }
        return tokenIssuer.issueTokenResponse(created);
    }

    private LoginPrincipal resolveOrRegister(String email, boolean allowAutoRegister) {
        Optional<LoginPrincipal> existing = userResolver.findByEmail(email);
        if (existing.isPresent()) {
            return existing.get();
        }
        if (allowAutoRegister && loginProperties.getRegister().isLoginAsRegister()) {
            LoginUserRegistrar registrar = userRegistrar.orElseThrow(() ->
                    new IllegalStateException(
                            "LoginUserRegistrar bean is required when login-as-register=true"));
            return registrar.register(RegisterRequest.forEmail(email));
        }
        throw LoginAuthException.userNotFound();
    }
}
