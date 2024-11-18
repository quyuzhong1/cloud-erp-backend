package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 虚拟仓渠道
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("virtual_warehouse_channel")
public class VirtualWarehouseChannelEntity extends BaseEntity<VirtualWarehouseChannelEntity> {
    /**
    * 虚拟仓id
    */
    @TableField("virtual_warehouse_id")
    private String virtualWarehouseId;
    /**
    * 平台的dict值
    */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
    * 关联类型：  platform 按平台 shop 按店铺 VitualWarehouseChannelTypeEnum
    */
    @TableField("type")
    private String type;
    /**
    * 关联id（例如店铺）
    */
    @TableField("relation_id")
    private String relationId;

    public static final String VIRTUAL_WAREHOUSE_ID = "virtual_warehouse_id";

    public static final String DICT_PLATFORM = "dict_platform";

    

    public static final String RELATION_ID = "relation_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}