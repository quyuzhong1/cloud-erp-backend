package com.erp.model.mrp.entity;

import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;

/**
 * <p>
 * 库存详情
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("replenishment_inventory_detail")
public class ReplenishmentInventoryDetailEntity extends BaseEntity<ReplenishmentInventoryDetailEntity> {

    /**
     * 补货建议id
     */
    @TableField("replenishment_detail_id")
    private String replenishmentDetailId;

    /**
     * 类型   海外仓可用/海外仓在途/预计发货/本地仓可用/本地仓在途/预计采购
     */
    @TableField("inventory_type")
    private String inventoryType;
    /**
     * 平台
     */
    @TableField("dict_platform")
    private String dictPlatform;
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
    @TableField(value = "channel_id_json",jdbcType = JdbcType.OTHER)
    private JSONArray channelIdJson;

    /**
     * 库存分配类型
     */
    @TableField("inventory_allocate_type")
    private String inventoryAllocateType;

    /**
     * 总数量
     */
    @TableField("total_qty")
    private Integer totalQty;

    /**
     * 计算版本  所有子表加   根据单号生成规则
     */
    @TableField("calc_version")
    private String calcVersion;


    public static final String REPLENISHMENT_DETAIL_ID = "replenishment_detail_id";

    public static final String INVENTORY_TYPE = "inventory_type";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_ID = "virtual_warehouse_id";

    public static final String WAREHOUSE_TYPE = "warehouse_type";

    public static final String CHANNEL_TYPE = "channel_type";

    public static final String CHANNEL_ID_JSON = "channel_id_json";

    public static final String INVENTORY_ALLOCATE_TYPE = "inventory_allocate_type";

    public static final String TOTAL_QTY = "total_qty";

    public static final String CALC_VERSION = "calc_version";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
