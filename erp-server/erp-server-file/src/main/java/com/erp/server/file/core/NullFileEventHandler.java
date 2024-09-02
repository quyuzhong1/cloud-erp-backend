package com.erp.server.file.core;

import com.erp.server.file.entity.FileTask;
import com.common.business.enums.FileTaskEventEnum;
import com.erp.server.file.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class NullFileEventHandler implements FileEventHandler {
    @Override
    public void handle(FileTask fileTask) {
        throw new BusinessException("未支持");
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.DEFAULT;
    }

}
