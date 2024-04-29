package com.cloud.erp.listener;

import com.cloud.erp.context.FileTaskContext;
import com.cloud.erp.event.FileTaskCreateEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class FileTaskCreateListener implements ApplicationListener<FileTaskCreateEvent> {
    @Resource
    private FileTaskContext fileTaskContext;

    @Override
    public void onApplicationEvent(@NonNull FileTaskCreateEvent fileTaskCreateEvent) {
        fileTaskContext.process(fileTaskCreateEvent.getFileTaskId());
    }
}
