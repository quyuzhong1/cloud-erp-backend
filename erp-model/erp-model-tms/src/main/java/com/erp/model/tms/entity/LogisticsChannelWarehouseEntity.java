package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 渠道仓库设置表
 * </p>
 *
 * @author will
 * @since 2024-05-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_channel_warehouse")
public class LogisticsChannelWarehouseEntity extends BaseEntity<LogisticsChannelWarehouseEntity> {

    /**
    * 渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 类型，all全部，part部分
    */
    @TableField("type")
    private String type;


    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String FIELD_TYPE = "type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}