package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Date;
import java.io.Serializable;

/**
 * 任务sku配置关系表(TaskRefSkuConfig)实体类
 *
 * @author Lambda
 * @since 2022-11-21 12:32:13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("task_ref_sku_config")
public class TaskRefSkuConfigEntity extends BaseEntity<TaskRefSkuConfigEntity> implements Serializable {
    private static final long serialVersionUID = 942529455380352989L;

    /**
     * 任务id
     */
    private String taskId;
    /**
     * 字段配置类型 createSku 创造sku，fillProductInfo 填写信息
     */
    private String fieldConfigType;
    /**
     * sku表id
     */
    private String skuId;
    /**
     * sku 编号
     */
    private String skuNo;
    /**
     * 产品id
     */
    private String productId;
    /**
     * 勾选字段后的json 字段
     */
    private String fieldJson;

}

