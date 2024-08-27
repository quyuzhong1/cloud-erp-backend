package com.erp.server.file.core;


import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.write.handler.WriteHandler;
import com.common.business.enums.FileTaskEventEnum;
import com.erp.server.file.entity.FileTask;

import java.util.Collections;
import java.util.List;

public interface FileEventHandler {

    /**
     * 处理文件任务
     *
     * @param fileTask 文件任务
     */
    void handle(FileTask fileTask);

    default boolean isMatch(String event) {
        return getEvent().name().equals(event);
    }

    /**
     * 任务来源
     * @return {@link FileTaskEventEnum}
     */
    FileTaskEventEnum getEvent();

    default List<WriteHandler> getWriteHandler() {
        return Collections.emptyList();
    }

    default List<Converter<?>> getConverter() {
        return Collections.emptyList();
    }
}