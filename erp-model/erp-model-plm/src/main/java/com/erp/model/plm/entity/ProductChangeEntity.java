package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 变更信息表(ProductChange)实体类
 *
 * @author yl
 * @since 2023-01-11 12:26:20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_change")
public class ProductChangeEntity implements Serializable {
    private static final long serialVersionUID = -79726809443887610L;


    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 创建人id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
     * 更新人id
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;
    /**
     * 类型 bom sku
     */
    private String type;
    /**
     * 源数据 如sku ，bom 表id
     */
    private String sourceId;
    /**
     * 状态
     */
    private Integer state;
    /**
     * 备注
     */
    private String remark;
    /**
     * 审核完成时间
     */
    private LocalDateTime approvalFinishTime;



}

