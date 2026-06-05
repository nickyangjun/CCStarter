package com.company.component.auth.login.spi;

import com.company.component.auth.login.core.LoginPrincipal;
import com.company.component.auth.login.core.RegisterRequest;

public interface LoginUserRegistrar {

    LoginPrincipal register(RegisterRequest request);
}
