package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 仓库（规则设置）明细
 * </p>
 *
 * @author will
 * @since 2024-08-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_rule_warehouse_detail")
public class CfgRuleWarehouseDetailEntity extends BaseEntity<CfgRuleWarehouseDetailEntity> {

    /**
    * 实体仓id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 虚拟仓id
    */
    @TableField("virtual_warehouse_id")
    private String virtualWarehouseId;
    /**
    * 仓库类型，local本地，overseas海外
    */
    @TableField("warehouse_type")
    private String warehouseType;
    /**
    * 关联店铺类型，platform按平台，shop按店铺
    */
    @TableField("channel_type")
    private String channelType;
    /**
    * 店铺id的json
    */
    @TableField("channel_id_json")
    private String channelIdJson;
    /**
    * 库存分配类型
    */
    @TableField("inventory_allocate_type")
    private String inventoryAllocateType;
    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;


    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_ID = "virtual_warehouse_id";

    public static final String WAREHOUSE_TYPE = "warehouse_type";

    public static final String CHANNEL_TYPE = "channel_type";

    public static final String CHANNEL_ID_JSON = "channel_id_json";

    public static final String INVENTORY_ALLOCATE_TYPE = "inventory_allocate_type";

    public static final String MAIN_ID = "main_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}