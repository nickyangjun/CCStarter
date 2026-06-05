package com.company.component.auth.login.support;

import com.company.component.auth.login.properties.LoginProperties;

import java.util.ArrayList;
import java.util.List;

public final class LoginPathCollector {

    private LoginPathCollector() {
    }

    public static List<String> collect(LoginProperties login) {
        List<String> paths = new ArrayList<>();
        if (!login.isEnabled()) {
            return paths;
        }
        if (login.getSms().isEnabled()) {
            LoginProperties.Paths smsPaths = login.getSms().getPaths();
            paths.add(smsPaths.getSendCode());
            paths.add(smsPaths.getLogin());
            if (login.getRegister().isEnabled()) {
                paths.add(smsPaths.getRegister());
            }
        }
        if (login.getEmail().isEnabled()) {
            LoginProperties.EmailPaths emailPaths = login.getEmail().getPaths();
            paths.add(emailPaths.getSendCode());
            paths.add(emailPaths.getLogin());
            if (login.getRegister().isEnabled()) {
                paths.add(emailPaths.getRegister());
            }
        }
        return paths;
    }
}
