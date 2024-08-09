package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.wms.enums.WmsDeclareStatusEnum;
import com.erp.model.wms.enums.FmDeliveryLogisticsStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * 头程发货单
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("first_mile_delivery")
public class FirstMileDeliveryEntity extends BaseEntity<FirstMileDeliveryEntity> {

    /**
    * code
    */
    @TableField("code")
    private String code;
    /**
    * 审核状态
    */
    @TableField("approve_status")
    private String approveStatus;
    /**
    * 审核时间
    */
    @TableField("approve_time")
    private LocalDateTime approveTime;
    /**
    * 审核人id
    */
    @TableField("approve_user_id")
    private String approveUserId;
    /**
    * 审核人
    */
    @TableField("approve_user_name")
    private String approveUserName;
    /**
    * 作废状态（false未作废，true已作废）
    */
    @TableField("invalid_status")
    private Boolean invalidStatus;
    /**
    * 作废原因
    */
    @TableField("invalid_remark")
    private String invalidRemark;
    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 来源编码
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 备货类型
    */
    @TableField("demand_type")
    private String demandType;
    /**
    * 店铺id
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 店铺名称
    */
    @TableField("shop_name")
    private String shopName;
    /**
    * 国家二字码
    */
    @TableField("country_id")
    private String countryId;
    /**
    * 国家名称
    */
    @TableField("country_name")
    private String countryName;
    /**
    * 发货仓id
    */
    @TableField("delivery_warehouse_id")
    private String deliveryWarehouseId;
    /**
    * 发货仓名称
    */
    @TableField("delivery_warehouse_name")
    private String deliveryWarehouseName;
    /**
    * 目的仓id
    */
    @TableField("dest_warehouse_id")
    private String destWarehouseId;
    /**
    * 目的仓名称
    */
    @TableField("dest_warehouse_name")
    private String destWarehouseName;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 平台物流中心
    */
    @TableField("fulfillment_center")
    private String fulfillmentCenter;
    /**
    * 库存组织id
    */
    @TableField("inventory_org_id")
    private String inventoryOrgId;
    /**
    * 库存组织名称
    */
    @TableField("inventory_org_name")
    private String inventoryOrgName;
    /**
    * 发货状态
    */
    @TableField("delivery_status")
    private String deliveryStatus;
    /**
    * 装箱状态 wait:未生成;unpacked:待装箱;packing:装箱中;packed:已装箱
     * PackingTaskStatusEnum
    */
    @TableField("packing_status")
    private String packingStatus;
    /**
    * 物流单状态：none：无需生成，wait：未生成，finish：已生成
    */
    @TableField("logistics_status")
    private FmDeliveryLogisticsStatusEnum logisticsStatus;
    /**
    * 报关单状态：none：无需生成，wait：未生成，finish：已生成
    */
    @TableField("declare_status")
    private WmsDeclareStatusEnum declareStatus;

    public static final String CODE = "code";

    public static final String APPROVE_STATUS = "approve_status";

    public static final String APPROVE_TIME = "approve_time";

    public static final String APPROVE_USER_ID = "approve_user_id";

    public static final String APPROVE_USER_NAME = "approve_user_name";

    public static final String INVALID_STATUS = "invalid_status";

    public static final String INVALID_REMARK = "invalid_remark";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_CODE = "source_code";

    public static final String DEMAND_TYPE = "demand_type";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String COUNTRY_ID = "country_id";

    public static final String COUNTRY_NAME = "country_name";

    public static final String DELIVERY_WAREHOUSE_ID = "delivery_warehouse_id";

    public static final String DELIVERY_WAREHOUSE_NAME = "delivery_warehouse_name";

    public static final String DEST_WAREHOUSE_ID = "dest_warehouse_id";

    public static final String DEST_WAREHOUSE_NAME = "dest_warehouse_name";

    public static final String REMARK = "remark";

    public static final String FULFILLMENT_CENTER = "fulfillment_center";

    public static final String INVENTORY_ORG_ID = "inventory_org_id";

    public static final String INVENTORY_ORG_NAME = "inventory_org_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}