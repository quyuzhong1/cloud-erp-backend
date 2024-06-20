package com.erp.model.wms.entity;

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
    * 虚拟仓编码
    */
    @TableField("virtual_warehouse_code")
    private String virtualWarehouseCode;
    /**
    * 虚拟仓名称
    */
    @TableField("virtual_warehouse_name")
    private String virtualWarehouseName;
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
    /**
    * 关联名称
    */
    @TableField("relation_name")
    private String relationName;
    /**
    * 平台类型
    */
    @TableField("dict_platform_type")
    private String dictPlatformType;


    public static final String DISABLED = "disabled";

    public static final String VIRTUAL_WAREHOUSE_ID = "virtual_warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_CODE = "virtual_warehouse_code";

    public static final String VIRTUAL_WAREHOUSE_NAME = "virtual_warehouse_name";

    public static final String DICT_PLATFORM = "dict_platform";

    public static final String TYPE = "type";

    public static final String RELATION_ID = "relation_id";

    public static final String RELATION_NAME = "relation_name";

    public static final String DICT_PLATFORM_TYPE = "dict_platform_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}