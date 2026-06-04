package com.company.component.auth.spi;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * 业务侧按用户名加载 {@link UserDetails}（可选 SPI）。
 */
public interface AuthUserDetailsLoader {

    UserDetails loadByUsername(String username);
}
