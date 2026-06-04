package com.company.component.log.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "component.log")
public class LogProperties {

    private boolean enabled = false;

    private final Trace trace = new Trace();

    private final Request request = new Request();

    private final Operation operation = new Operation();

    private final Mask mask = new Mask();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Trace getTrace() {
        return trace;
    }

    public Request getRequest() {
        return request;
    }

    public Operation getOperation() {
        return operation;
    }

    public Mask getMask() {
        return mask;
    }

    public static class Trace {

        private List<String> headerNames = new ArrayList<>(List.of("X-Trace-Id"));

        private boolean allowLocalGenerate = true;

        private boolean responseHeader = true;

        public List<String> getHeaderNames() {
            return headerNames;
        }

        public void setHeaderNames(List<String> headerNames) {
            this.headerNames = headerNames != null ? headerNames : new ArrayList<>();
        }

        public boolean isAllowLocalGenerate() {
            return allowLocalGenerate;
        }

        public void setAllowLocalGenerate(boolean allowLocalGenerate) {
            this.allowLocalGenerate = allowLocalGenerate;
        }

        public boolean isResponseHeader() {
            return responseHeader;
        }

        public void setResponseHeader(boolean responseHeader) {
            this.responseHeader = responseHeader;
        }
    }

    public static class Request {

        private boolean enabled = true;

        private String logLevel = "DEBUG";

        private double sampleRate = 1.0d;

        private boolean includeQuery = false;

        private List<String> excludePaths = new ArrayList<>(List.of("/actuator/**"));

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getLogLevel() {
            return logLevel;
        }

        public void setLogLevel(String logLevel) {
            this.logLevel = logLevel;
        }

        public double getSampleRate() {
            return sampleRate;
        }

        public void setSampleRate(double sampleRate) {
            this.sampleRate = sampleRate;
        }

        public boolean isIncludeQuery() {
            return includeQuery;
        }

        public void setIncludeQuery(boolean includeQuery) {
            this.includeQuery = includeQuery;
        }

        public List<String> getExcludePaths() {
            return excludePaths;
        }

        public void setExcludePaths(List<String> excludePaths) {
            this.excludePaths = excludePaths != null ? excludePaths : new ArrayList<>();
        }
    }

    public static class Operation {

        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Mask {

        private List<String> keys = new ArrayList<>(List.of("password", "token", "authorization"));

        public List<String> getKeys() {
            return keys;
        }

        public void setKeys(List<String> keys) {
            this.keys = keys != null ? keys : new ArrayList<>();
        }
    }
}
