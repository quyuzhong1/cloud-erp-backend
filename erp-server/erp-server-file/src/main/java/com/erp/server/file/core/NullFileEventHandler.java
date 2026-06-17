package com.erp.server.file.core;

import com.erp.model.file.entity.FileTask;
import com.common.business.enums.FileTaskEventEnum;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Component;

@Component
public class NullFileEventHandler implements FileEventHandler {
    @Override
    public void handle(FileTask fileTask) {
        throw new ServiceException("未支持");
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.DEFAULT;
    }

}
