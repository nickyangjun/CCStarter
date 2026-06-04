package com.company.component.log.spi;

import com.company.component.log.operation.OperationLogEntry;

/**
 * 业务操作日志落库 SPI；实现方应异步持久化，不得阻塞主链路。
 */
public interface OperationLogRecorder {

    void record(OperationLogEntry entry);
}
