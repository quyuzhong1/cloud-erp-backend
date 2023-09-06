package com.erp.model.dmp.entity;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 
 * @TableName platform_api_task
 */
@NoArgsConstructor
@TableName(value ="platform_api_task")
@Data
public class PlatformApiTaskEntity extends BaseEntity<PlatformApiTaskEntity> {

    /**
     * 平台编码
     */
    @TableField(value = "dict_platform")
    private String dictPlatform;

    /**
     * 平台api接口
     */
    @TableField(value = "api_code")
    private String apiCode;

    /**
     * 平台api名称
     */
    @TableField(value = "api_name")
    private String apiName;

    /**
     * 间隔时间 单位：秒
     */
    @TableField(value = "interval_time")
    private Integer intervalTime;

    /**
     * 上次执行时间
     */
    @TableField(value = "last_time")
    private LocalDateTime lastTime;

    /**
     * 下次执行时间
     */
    @TableField(value = "next_time")
    private LocalDateTime nextTime;

    /**
     * 任务状态：1：待拉取  2：拉取中
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 重试次数
     */
    @TableField(value = "retry_times")
    private Integer retryTimes;

    /**
     * 店铺名称
     */
    @TableField(value = "shop_name")
    private String shopName;

    /**
     * 店铺id
     */
    @TableField(value = "shop_id")
    private String shopId;

    /**
     * 单据类型
     */
    @TableField(value = "bill_type")
    private String billType;

    /**
     * 操作类型
     */
    @TableField(value = "operate_type")
    private String operateType;

    /**
     * api参数
     */
    @TableField(value = "api_param", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> apiParam;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 错误信息
     */
    @TableField(value = "error_msg")
    private String errorMsg;

    /**
     * 禁用
     */
    @TableField(value = "disabled")
    private Boolean disabled;

    /**
     * 平台api表id
     */
    @TableField(value = "platform_api_id")
    private String platformApiId;

    /**
     * 平台分类
     */
    @TableField(value = "platform_category")
    private String platformCategory;

    /**
     * 同步操作
     */
    @TableField(value = "sync_operate")
    private String syncOperate;

    /**
     * api参数
     */

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public PlatformApiTaskEntity(PlatformApiEntity item, String shopId, String shopName) {
        this.dictPlatform = item.getDictPlatform();
        this.apiCode = item.getApiCode();
        this.apiName = item.getApiName();
        this.intervalTime = item.getIntervalTime();
        this.lastTime = LocalDateTimeUtil.beginOfDay(LocalDateTime.now());
        this.nextTime = LocalDateTimeUtil.endOfDay(LocalDateTime.now());
        this.shopName = shopName;
        this.shopId = shopId;
        this.apiParam = item.getApiCommonParam();
        this.billType = item.getBillType();
        this.operateType = item.getOperateType();
        this.platformApiId = item.getId();
        this.syncOperate = item.getSyncOperate();
    }
}