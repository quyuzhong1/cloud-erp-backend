package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * FBA货件状态信息
 * </p>
 *
 * @author Jim
 * @since 2023-11-01
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("fba_shipment_status")
public class FbaShipmentStatusEntity extends BaseEntity<FbaShipmentStatusEntity> {

    /**
    * FBA货件id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 平台货件状态
    */
    @TableField("platform_shipment_status")
    private String platformShipmentStatus;
    /**
    * 亚马逊FBA货件单号
    */
    @TableField("fba_shipment_id")
    private String fbaShipmentId;


    public static final String MAIN_ID = "main_id";

    public static final String PLATFORM_SHIPMENT_STATUS = "platform_shipment_status";

    public static final String FBA_SHIPMENT_ID = "fba_shipment_id";

}