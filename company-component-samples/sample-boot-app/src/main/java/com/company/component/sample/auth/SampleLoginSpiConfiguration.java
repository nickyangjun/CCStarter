package com.company.component.sample.auth;

import com.company.component.auth.login.core.LoginPrincipal;
import com.company.component.auth.login.core.RegisterRequest;
import com.company.component.auth.login.spi.LoginUserRegistrar;
import com.company.component.auth.login.spi.LoginUserResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class SampleLoginSpiConfiguration {

    @Bean
    SampleUserStore sampleUserStore() {
        return new SampleUserStore();
    }

    @Bean
    LoginUserResolver sampleLoginUserResolver(SampleUserStore store) {
        return new LoginUserResolver() {
            @Override
            public Optional<LoginPrincipal> findByMobile(String mobile) {
                return store.findByMobile(mobile);
            }

            @Override
            public Optional<LoginPrincipal> findByEmail(String email) {
                return store.findByEmail(email);
            }
        };
    }

    @Bean
    LoginUserRegistrar sampleLoginUserRegistrar(SampleUserStore store) {
        return request -> {
            if (StringUtils.hasText(request.email())) {
                return store.registerByEmail(request.email());
            }
            return store.registerByMobile(request.mobile());
        };
    }

    static final class SampleUserStore {

        private final AtomicLong idSeq = new AtomicLong(1000);
        private final Map<String, LoginPrincipal> byMobile = new ConcurrentHashMap<>();
        private final Map<String, LoginPrincipal> byEmail = new ConcurrentHashMap<>();

        Optional<LoginPrincipal> findByMobile(String mobile) {
            return Optional.ofNullable(byMobile.get(mobile));
        }

        Optional<LoginPrincipal> findByEmail(String email) {
            return Optional.ofNullable(byEmail.get(email));
        }

        LoginPrincipal registerByMobile(String mobile) {
            LoginPrincipal principal = new LoginPrincipal(mobile, idSeq.incrementAndGet());
            byMobile.put(mobile, principal);
            return principal;
        }

        LoginPrincipal registerByEmail(String email) {
            LoginPrincipal principal = new LoginPrincipal(email, idSeq.incrementAndGet());
            byEmail.put(email, principal);
            return principal;
        }
    }
}
