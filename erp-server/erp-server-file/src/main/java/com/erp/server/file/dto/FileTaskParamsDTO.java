package com.erp.server.file.dto;

import com.erp.server.file.enums.FileTaskEventEnum;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class FileTaskParamsDTO {

    /**
     * 事件类型
     * @see FileTaskEventEnum
     */
    private String event;
}
