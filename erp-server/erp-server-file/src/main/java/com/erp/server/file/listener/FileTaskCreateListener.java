package com.erp.server.file.listener;

import com.erp.server.file.context.FileTaskContext;
import com.erp.server.file.event.FileTaskCreateEvent;
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
