package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
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
public class ProductChangeEntity extends BaseEntity<ProductChangeEntity> implements Serializable {
    private static final long serialVersionUID = -79726809443887610L;

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

