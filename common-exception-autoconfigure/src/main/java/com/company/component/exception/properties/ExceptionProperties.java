package com.company.component.exception.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 统一异常组件配置。生产环境请显式设置 {@code component.exception.enabled=true}。
 */
@Validated
@ConfigurationProperties(prefix = "component.exception")
public class ExceptionProperties {

    /**
     * 总开关，默认关闭。
     */
    private boolean enabled = false;

    /**
     * 响应是否包含请求路径。
     */
    private boolean includePath = true;

    /**
     * 是否在响应中暴露堆栈（仅开发环境，生产必须为 false）。
     */
    private boolean exposeStackTrace = false;

    /**
     * 未识别异常的兜底错误码。
     */
    private String defaultErrorCode = "INTERNAL_ERROR";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isIncludePath() {
        return includePath;
    }

    public void setIncludePath(boolean includePath) {
        this.includePath = includePath;
    }

    public boolean isExposeStackTrace() {
        return exposeStackTrace;
    }

    public void setExposeStackTrace(boolean exposeStackTrace) {
        this.exposeStackTrace = exposeStackTrace;
    }

    public String getDefaultErrorCode() {
        return defaultErrorCode;
    }

    public void setDefaultErrorCode(String defaultErrorCode) {
        this.defaultErrorCode = defaultErrorCode;
    }
}
