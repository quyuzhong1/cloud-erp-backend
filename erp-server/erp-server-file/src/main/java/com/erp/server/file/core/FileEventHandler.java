package com.erp.server.file.core;


import com.erp.server.file.entity.FileTask;
import com.common.business.enums.FileTaskEventEnum;

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


}