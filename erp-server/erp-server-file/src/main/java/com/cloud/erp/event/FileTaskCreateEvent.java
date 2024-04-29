package com.cloud.erp.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FileTaskCreateEvent  extends ApplicationEvent {

    private final String fileTaskId;
    public FileTaskCreateEvent(String fileTaskId) {
        super(fileTaskId);
        this.fileTaskId = fileTaskId;
    }

}
