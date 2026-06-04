package com.company.component.auth.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 鉴权组件配置，前缀 {@code component.auth}。
 */
@ConfigurationProperties(prefix = "component.auth")
public class AuthProperties {

    private boolean enabled = false;

    /**
     * HMAC 密钥，enabled=true 时由自动配置校验（建议 ≥ 32 字符）。
     */
    private String jwtSecret;

    private int expireMinutes = 120;

    private String headerName = "Authorization";

    private String tokenPrefix = "Bearer ";

    private List<String> whitelist = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public int getExpireMinutes() {
        return expireMinutes;
    }

    public void setExpireMinutes(int expireMinutes) {
        this.expireMinutes = expireMinutes;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public List<String> getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist != null ? whitelist : new ArrayList<>();
    }
}
