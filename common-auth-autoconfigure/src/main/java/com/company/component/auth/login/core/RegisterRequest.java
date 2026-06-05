package com.company.component.auth.login.core;

import java.util.Collections;
import java.util.Map;

public record RegisterRequest(String mobile, String email, Map<String, Object> attributes) {

    public static RegisterRequest forMobile(String mobile) {
        return new RegisterRequest(mobile, null, Collections.emptyMap());
    }

    public static RegisterRequest forEmail(String email) {
        return new RegisterRequest(null, email, Collections.emptyMap());
    }

    public RegisterRequest(String mobile) {
        this(mobile, null, Collections.emptyMap());
    }
}
