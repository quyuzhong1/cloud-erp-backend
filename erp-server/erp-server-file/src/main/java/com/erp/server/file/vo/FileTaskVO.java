package com.erp.server.file.vo;

import com.erp.server.file.enums.FileTaskEventEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FileTaskVO {
    /**
     * 主键
     */
    private String id;

    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 创建人名称
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改人id
     */
    private String updateUserId;

    /**
     * 修改人名称
     */
    private String updateUserName;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 任务事件名称
     * @see FileTaskEventEnum
     */
    private String event;
    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 文件的URL
     */
    private String fileUrl;
    /**
     * 任务状态
     * PENDING-等待中
     * PROCESS-处理中
     * FINISH 全部成功
     * PART 部分成功
     * FAIL 全部失败
     */
    private String status;
    /**
     * 任务的备注信息
     */
    private String remarks;
    /**
     * 任务开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
    /**
     * 任务结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime finishTime;
}