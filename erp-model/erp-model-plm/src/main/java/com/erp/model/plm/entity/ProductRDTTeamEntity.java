package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * 产品研发团队表
 * @TableName product_rdt_team
 */
@TableName(value ="product_rdt_team")
@Data
public class ProductRDTTeamEntity extends BaseEntity<ProductRDTTeamEntity> implements Serializable {

    /**
     * 研发团队名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 是否占用 默认 false  占用为true 就不能删除
     */
    @TableField(value = "occupy_status")
    private Boolean occupyStatus;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

