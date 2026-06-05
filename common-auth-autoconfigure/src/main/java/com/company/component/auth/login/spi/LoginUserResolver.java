package com.company.component.auth.login.spi;

import com.company.component.auth.login.core.LoginPrincipal;

import java.util.Optional;

public interface LoginUserResolver {

    Optional<LoginPrincipal> findByMobile(String mobile);

    default Optional<LoginPrincipal> findByEmail(String email) {
        return Optional.empty();
    }
}
