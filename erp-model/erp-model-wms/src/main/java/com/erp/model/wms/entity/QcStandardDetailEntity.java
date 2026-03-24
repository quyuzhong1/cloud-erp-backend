package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 质检项目明细表
 *
 * @author jack
 * @since 2026-03-22
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("qc_standard_detail")
public class QcStandardDetailEntity extends BaseEntity<QcStandardDetailEntity> {

    /**
     * 关联的主表id
     */
    @TableField("main_id")
    private String mainId;

    /**
     * 序号
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 质检项目
     */
    @TableField("inspect_item_name")
    private String inspectItemName;

    /**
     * 质检要求
     */
    @TableField("inspect_requirement")
    private String inspectRequirement;

    @Override
    public Serializable pkVal() {
        return this.getId();
    }

}
