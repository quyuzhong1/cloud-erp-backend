package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * FBT货件主表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_fbt_shipment")
public class DmpFbtShipmentEntity extends BaseEntity<DmpFbtShipmentEntity> {

    @TableField("source_platform")
    private String sourcePlatform;

    @TableField("auth_id")
    private String authId;

    @TableField("shop_id")
    private String shopId;

    @TableField("inbound_order_id")
    private String inboundOrderId;

    @TableField("shipment_name")
    private String shipmentName;

    @TableField("platform_warehouse_code")
    private String platformWarehouseCode;

    @TableField("platform_warehouse_name")
    private String platformWarehouseName;

    @TableField("platform_shipment_status")
    private String platformShipmentStatus;

    @TableField("carrier_list_json")
    private String carrierListJson;

    @TableField("received_batches_json")
    private String receivedBatchesJson;

    @TableField("platform_update_time")
    private LocalDateTime platformUpdateTime;

    @TableField("input_task_id")
    private String inputTaskId;

    @TableField("convert_id")
    private String convertId;

    @TableField("next_level_id")
    private String nextLevelId;

    @TableField("unique_encrypt")
    private String uniqueEncrypt;

    @TableField("data_encrypt")
    private String dataEncrypt;
}
