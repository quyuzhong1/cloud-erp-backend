package com.cloud.erp.core;

import com.cloud.erp.entity.FileTask;
import com.cloud.erp.enums.FileTaskEventEnum;
import com.cloud.erp.exception.BusinessException;
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
