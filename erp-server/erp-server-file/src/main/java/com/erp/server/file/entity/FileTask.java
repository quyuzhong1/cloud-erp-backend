package com.erp.server.file.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.enums.FileTaskEventEnum;
import com.erp.server.file.enums.FileTaskStatusEnum;
import com.common.core.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("file_task")
@EqualsAndHashCode(callSuper = false)
public class FileTask extends BaseEntity<FileTask> {

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
     */
    private String status;
    /**
     * 元数据信息
     */
    private String metaInfo;
    /**
     * 任务的备注信息
     */
    private String remark;
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
    /**
     * 数据总条数
     */
    private Integer count;
    /**
     * 类型 auto 自动 manual 手动
     */
    private String type;

    public static FileTask create(String event, String fileName, String metaInfo) {
        FileTask task = new FileTask();
        // 事件名称
        task.setEvent(event);
        // 文件
        task.setFileName(fileName);
        // 创建时候状态默认为等待中
        task.setStatus(FileTaskStatusEnum.PENDING.name());
        // 元数据信息
        task.setMetaInfo(metaInfo);
        return task;
    }


    /**
     * 非稳定状态
     */
    public boolean volatileStatus() {
        return FileTaskStatusEnum.PROCESS.name().equals(status);
    }


    /**
     * 等待中
     */
    public boolean isPending() {
        return FileTaskStatusEnum.PENDING.name().equals(status);
    }
}
