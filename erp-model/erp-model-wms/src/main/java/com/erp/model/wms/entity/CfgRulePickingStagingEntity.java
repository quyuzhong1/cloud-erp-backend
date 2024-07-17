package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 拣货暂存规则
 * </p>
 *
 * @author liaohui
 * @since 2024-06-07
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("cfg_rule_picking_staging")
public class CfgRulePickingStagingEntity extends BaseEntity<CfgRulePickingStagingEntity> {

    /**
     * 仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 业务类型 	B2B B2B订单	FBA FBA头程要货单	THIRD 三方仓头程要货单
     */
    @TableField("bill_type")
    private String billType;

    /**
     * 库区id
     */
    @TableField("warehouse_area_id")
    private String warehouseAreaId;

    /**
     * 仓位id
     */
    @TableField("warehouse_location_id")
    private String warehouseLocationId;

    /**
     * 库位
     */
    @TableField("warehouse_location")
    private String warehouseLocation;


    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String BILL_TYPE = "bill_type";

    public static final String WAREHOUSE_AREA_ID = "warehouse_area_id";

    public static final String WAREHOUSE_LOCATION_ID = "warehouse_location_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
