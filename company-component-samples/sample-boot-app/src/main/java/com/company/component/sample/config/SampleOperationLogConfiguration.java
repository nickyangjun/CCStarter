package com.company.component.sample.config;

import com.company.component.log.operation.OperationLogEntry;
import com.company.component.log.spi.OperationLogRecorder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
public class SampleOperationLogConfiguration {

    private final List<OperationLogEntry> entries = Collections.synchronizedList(new ArrayList<>());

    @Bean
    public OperationLogRecorder sampleOperationLogRecorder() {
        return entries::add;
    }

    public List<OperationLogEntry> getEntries() {
        return List.copyOf(entries);
    }

    public void clearEntries() {
        entries.clear();
    }
}
