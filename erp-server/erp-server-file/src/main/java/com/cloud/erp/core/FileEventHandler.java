package com.cloud.erp.core;


import com.cloud.erp.entity.FileTask;
import com.cloud.erp.enums.FileTaskEventEnum;

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

    FileTaskEventEnum getEvent();

}