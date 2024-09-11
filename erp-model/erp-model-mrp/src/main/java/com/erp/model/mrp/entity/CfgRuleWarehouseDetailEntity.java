package com.erp.model.mrp.entity;

import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.util.List;


/**
 * <p>
 * 仓库（规则设置）明细
 * </p>
 *
 * @author will
 * @since 2024-08-24
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
    * 渠道（店铺）id的json
    */
    @TableField(value = "channel_id_json", jdbcType = JdbcType.OTHER)
    private JSONArray channelIdJson;
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

    /**
     * 平台
     */
    @TableField("dict_platform")
    private String dictPlatform;

    /**
     * 渠道id集合
     */
    @TableField(exist = false)
    private List<String> channelIdList;

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