package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
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
public class TaskRefSkuConfigEntity implements Serializable {
    private static final long serialVersionUID = 942529455380352989L;
    /**
     * 表id
     */
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private String id;
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
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
     * 创建人
     */
    @TableField(value = "create_user_id")
    private String createUserId;
    /**
     * 更改人
     */
    @TableField(value = "update_user_id")
    private String updateUserId;



}

