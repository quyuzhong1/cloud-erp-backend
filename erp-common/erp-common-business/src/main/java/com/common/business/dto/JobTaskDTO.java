package com.common.business.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 任务信息实体类
 */
@Data
@NoArgsConstructor
public class JobTaskDTO {
    /**
     * 主键id
     */
    private String id;

    /**
     * 平台分类 PlatformCategoryEnum
     */
    private String platformCategory;

    /**
     * 平台表id PlatformDictEnum
     */
    private String dictPlatform;

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
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 平台api接口
     */
    private String apiCode;

    /**
     * 平台api名称
     */
    private String apiName;

    /**
     * 重试次数
     */
    private Integer retryTimes;

    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 业务类型 BusinessTypeEnum
     */
    private String billType;

    /**
     * 操作类型
     */
    private String operateType;

    /**
     * 任务名称
     */
    private Map<String, Object> apiParam;

    /**
     * 平台api接口id
     */
    private String platformApiId;

    /**
     * 平台api接口id
     */
    private List<?> mongoDataList;


//    public JobTaskDTO(PlatformApiTaskEntity entity, String taskName) {
//        this.id = Long.parseLong(entity.getId());
//        this.dictPlatform = entity.getDictPlatform();
//        this.intervalTime = entity.getIntervalTime();
//        this.lastTime = entity.getLastTime();
//        this.nextTime = entity.getNextTime();
//        this.status = entity.getStatus();
//        this.createTime = entity.getCreateTime();
//        this.apiCode = entity.getApiCode();
//        this.apiName = entity.getApiName();
//        this.retryTimes = entity.getRetryTimes();
//        this.shopName = entity.getShopName();
//        this.shopId = entity.getShopId();
//        this.billType = entity.getBillType();
//        this.operateType = entity.getOperateType();
//        this.apiParam = entity.getApiParam();
//    }

    public JobTaskDTO(JobTaskDTO tbTask) {
        this.id = tbTask.getId();
        this.dictPlatform = tbTask.getDictPlatform();
        this.intervalTime = tbTask.getIntervalTime();
        this.lastTime = tbTask.getLastTime();
        this.nextTime = tbTask.getNextTime();
        this.status = tbTask.getStatus();
        this.createTime = tbTask.getCreateTime();
        this.apiCode = tbTask.getApiCode();
        this.apiName = tbTask.getApiName();
        this.retryTimes = tbTask.getRetryTimes();
        this.shopName = tbTask.getShopName();
        this.shopId = tbTask.getShopId();
        this.billType = tbTask.getBillType();
        this.operateType = tbTask.getOperateType();
        this.apiParam = tbTask.getApiParam();
        this.platformCategory = tbTask.getPlatformCategory();
    }
}
