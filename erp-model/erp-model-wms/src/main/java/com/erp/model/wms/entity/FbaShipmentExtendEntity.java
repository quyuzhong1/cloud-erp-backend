package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * FBA拣货扩展表
 * </p>
 *
 * @author zdy
 * @since 2025-12-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("fba_shipment_extend")
public class FbaShipmentExtendEntity extends BaseEntity<FbaShipmentExtendEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 入库计划单号
    */
    @TableField("plan_code")
    private String planCode;
    /**
    * 发货单号
    */
    @TableField("delivery_code")
    private String deliveryCode;
    /**
    * 发货单ID
    */
    @TableField("delivery_id")
    private String deliveryId;
    /**
    * 地区偏好
    */
    @TableField("preferred_region")
    private String preferredRegion;
    /**
    * 发货人
    */
    @TableField("delivery_from_name")
    private String deliveryFromName;
    /**
    * 发货手机号
    */
    @TableField("delivery_from_mobile")
    private String deliveryFromMobile;
    /**
    * 发货城市
    */
    @TableField("delivery_from_city")
    private String deliveryFromCity;
    /**
    * 发货州/省
    */
    @TableField("delivery_from_province")
    private String deliveryFromProvince;
    /**
    * 发货地区
    */
    @TableField("delivery_from_area")
    private String deliveryFromArea;
    /**
    * 发货邮编
    */
    @TableField("delivery_from_post_code")
    private String deliveryFromPostCode;
    /**
    * 发货目的仓（取值店铺绑定的AWD仓）
    */
    @TableField("delivery_to_warehouse_id")
    private String deliveryToWarehouseId;
    /**
    * 平台货件发货时间（拉取数据的日期）
    */
    @TableField("shipment_delivery_time")
    private LocalDateTime shipmentDeliveryTime;
    /**
    * 收货电话号码
    */
    @TableField("delivery_to_mobile")
    private String deliveryToMobile;
    /**
    * 收货人
    */
    @TableField("delivery_to_name")
    private String deliveryToName;
    /**
    * 收货邮编
    */
    @TableField("delivery_to_post_code")
    private String deliveryToPostCode;
    /**
    * 收货地区
    */
    @TableField("delivery_to_area")
    private String deliveryToArea;
    /**
    * 收货州/省
    */
    @TableField("delivery_to_province")
    private String deliveryToProvince;
    /**
    * 收货城市
    */
    @TableField("delivery_to_city")
    private String deliveryToCity;
    /**
    * 收货国家
    */
    @TableField("delivery_to_country_id")
    private String deliveryToCountryId;
    /**
    * 收货国家名称
    */
    @TableField("delivery_to_country_name")
    private String deliveryToCountryName;


    public static final String MAIN_ID = "main_id";

    public static final String PLAN_CODE = "plan_code";

    public static final String DELIVERY_CODE = "delivery_code";

    public static final String DELIVERY_ID = "delivery_id";

    public static final String PREFERRED_REGION = "preferred_region";

    public static final String DELIVERY_FROM_NAME = "delivery_from_name";

    public static final String DELIVERY_FROM_MOBILE = "delivery_from_mobile";

    public static final String DELIVERY_FROM_CITY = "delivery_from_city";

    public static final String DELIVERY_FROM_PROVINCE = "delivery_from_province";

    public static final String DELIVERY_FROM_AREA = "delivery_from_area";

    public static final String DELIVERY_FROM_POST_CODE = "delivery_from_post_code";

    public static final String DELIVERY_TO_WAREHOUSE_ID = "delivery_to_warehouse_id";

    public static final String SHIPMENT_DELIVERY_TIME = "shipment_delivery_time";

    public static final String DELIVERY_TO_MOBILE = "delivery_to_mobile";

    public static final String DELIVERY_TO_NAME = "delivery_to_name";

    public static final String DELIVERY_TO_POST_CODE = "delivery_to_post_code";

    public static final String DELIVERY_TO_AREA = "delivery_to_area";

    public static final String DELIVERY_TO_PROVINCE = "delivery_to_province";

    public static final String DELIVERY_TO_CITY = "delivery_to_city";

    public static final String DELIVERY_TO_COUNTRY_ID = "delivery_to_country_id";

    public static final String DELIVERY_TO_COUNTRY_NAME = "delivery_to_country_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}