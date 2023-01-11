package com.erp.model.dmp.dto;

import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private LocalDateTime lastTime;

    /**
     * 下次执行时间
     */
    private LocalDateTime nextTime;

    /**
     * 任务状态：1：待拉取  2：拉取中  3:xxljob直接执行
     */
    private Integer state;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

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

    public JobTaskDTO(PlatformApiTaskEntity entity) {
        this.id = Long.parseLong(entity.getId());
        this.platformId = entity.getPlatformId();
        this.apiId = entity.getApiId();
        this.intervalTime = entity.getIntervalTime();
        this.lastTime = entity.getLastTime();
        this.nextTime = entity.getNextTime();
        this.state = entity.getState();
        this.createTime = entity.getCreateTime();
        this.apiCode = entity.getApiCode();
        this.apiName = entity.getApiName();
        this.taskName = "GYY_PULL_DATA_TASK";
        this.platformName = "管易云";
        this.errorCount = 0;
    }
}
