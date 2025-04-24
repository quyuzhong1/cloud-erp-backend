package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 产品操作记录表
 * @TableName product_operate_record
 */
@TableName(value ="product_operate_log")
@Data
public class ProductOperateRecordEntity extends BaseEntity<ProductOperateRecordEntity> implements Serializable {

    /**
     * 产品表id
     */
    @TableField(value = "product_id")
    private String productId;

    /**
     * 记录描述
     */
    @TableField(value = "remark")
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}