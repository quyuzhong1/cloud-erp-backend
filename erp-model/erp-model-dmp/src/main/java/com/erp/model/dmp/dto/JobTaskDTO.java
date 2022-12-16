package com.erp.model.dmp.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 任务信息实体类
 */
@Data
@NoArgsConstructor
public class JobTaskDTO {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 平台表id
     */
    private Integer platformId;

    /**
     * 平台接口表id
     */
    private Integer apiId;

    /**
     * 间隙时间
     */
    private Integer intervalTime;

    /**
     * 上次执行时间
     */
    private Integer lastTime;

    /**
     * 下次执行时间
     */
    private Integer nextTime;

    /**
     * 任务状态：1：待拉取  2：拉取中
     */
    private Integer state;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 平台api接口
     */
    private String apiCode;

    /**
     * 平台api名称
     */
    private String apiName;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 平台名称
     */
    private String platformName;

    /**
     * 错误次数
     */
    private Integer errorCount;
}
