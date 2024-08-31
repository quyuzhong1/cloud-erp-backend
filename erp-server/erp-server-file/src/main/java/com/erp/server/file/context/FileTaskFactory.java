package com.erp.server.file.context;


import com.erp.server.file.core.FileEventHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class FileTaskFactory {

    @Resource
    private List<FileEventHandler> handlers;

    /**
     * 根据事件名获取具体处理对象
     * @param event 事件名，
     */
    public FileEventHandler getFileHandler(String event) {
        for (FileEventHandler handler : handlers) {
            if (handler.isMatch(event)) {
                return handler;
            }
        }
        return null;
    }

}