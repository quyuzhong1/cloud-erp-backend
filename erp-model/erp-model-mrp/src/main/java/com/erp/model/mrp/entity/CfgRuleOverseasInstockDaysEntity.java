package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 海外仓入库天数明细
 * </p>
 *
 * @author liaohui
 * @since 2025-02-13
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("cfg_rule_overseas_instock_days")
public class CfgRuleOverseasInstockDaysEntity extends BaseEntity<CfgRuleOverseasInstockDaysEntity> {

    /**
     * 时效表cfg_rule_expire_time主键id
     */
    @TableField("expire_time_id")
    private String expireTimeId;

    /**
     * 仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 入库天数（天）
     */
    @TableField("instock_days")
    private Integer instockDays;

    /**
     * 排序字段
     */
    @TableField("index")
    private Integer index;


    public static final String EXPIRE_TIME_ID = "expire_time_id";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String INSTOCK_DAYS = "instock_days";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
