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
    /**
     * 是否只查询自己的
     */
    private Boolean owner;
    /**
     * 当前登录用户
     */
    private String createUserId;
}
