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
 * 预报设置-自动生成
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("transfer_declare_generation_setting")
public class TransferDeclareGenerationSettingEntity extends BaseEntity<TransferDeclareGenerationSettingEntity> {


    /**
    * 中转物流商id
    */
    @TableField("transfer_logistics_supplier_id")
    private String transferLogisticsSupplierId;
    /**
    * 中转物流服务商
    */
    @TableField("transfer_logistics_supplier_name")
    private String transferLogisticsSupplierName;
    /**
    * 中转渠道id
    */
    @TableField("transfer_channel_id")
    private String transferChannelId;
    /**
    * 中转渠道中文
    */
    @TableField("transfer_channel_name")
    private String transferChannelName;


    public static final String DELIVERY_LOGISTICS_SUPPLIER_ID = "delivery_logistics_supplier_id";

    public static final String DELIVERY_LOGISTICS_SUPPLIER_NAME = "delivery_logistics_supplier_name";

    public static final String TRANSFER_LOGISTICS_SUPPLIER_ID = "transfer_logistics_supplier_id";

    public static final String TRANSFER_LOGISTICS_SUPPLIER_NAME = "transfer_logistics_supplier_name";

    public static final String TRANSFER_CHANNEL_ID = "transfer_channel_id";

    public static final String TRANSFER_CHANNEL_NAME = "transfer_channel_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}