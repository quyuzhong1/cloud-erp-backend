package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 质检标准主表
 *
 * @author jack
 * @since 2026-03-22
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("qc_standard")
public class QcStandardEntity extends BaseEntity<QcStandardEntity> {

    /**
     * SKU内部ID
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * SKU编码
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 状态 false=启用 true:禁用
     */
    @TableField("disabled")
    private Boolean disabled;

    @Override
    public Serializable pkVal() {
        return this.getId();
    }

}
