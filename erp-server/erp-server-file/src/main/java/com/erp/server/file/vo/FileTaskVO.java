package com.erp.server.file.vo;

import com.common.business.annotation.Dict;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.erp.server.file.enums.FileTaskTypeEnum;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    /**
     * 任务事件
     * @see FileTaskEventEnum
     */
    @Dict(enumClass = FileTaskEventEnum.class)
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
     * 元数据信息
     */
    private String metaInfo;
    /**
     * 任务状态
     */
    @Dict(enumClass = FileTaskStatusEnum.class)
    private String status;
    /**
     * 任务的备注信息
     */
    private String remark;
    /**
     * 任务的处理数量
     */
    private Integer count;
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
     * 任务类型
     */
    @Dict(enumClass = FileTaskTypeEnum.class)
    private String type;
}