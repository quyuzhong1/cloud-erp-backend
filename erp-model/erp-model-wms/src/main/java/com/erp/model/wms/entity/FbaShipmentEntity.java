package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * FBI货件表
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("fba_shipment")
public class FbaShipmentEntity extends BaseEntity<FbaShipmentEntity> {

    /**
    * 单据编号
    */
    @TableField("code")
    private String code;
    /**
    * FBA货件名称
    */
    @TableField("name")
    private String name;
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
    * 平台物流中心
    */
    @TableField("fulfillment_center")
    private String fulfillmentCenter;
    /**
    * 发货状态
    */
    @TableField("delivery_status")
    private String deliveryStatus;
    /**
    * 平台货件状态
    */
    @TableField("platform_shipment_status")
    private String platformShipmentStatus;
    /**
    * 创建时间（拉取数据的日期）
    */
    @TableField("shipment_create_time")
    private LocalDateTime shipmentCreateTime;
    /**
    * 签收时间（拉取签收数据的日期）
    */
    @TableField("shipment_receive_time")
    private LocalDateTime shipmentReceiveTime;
    /**
    * 标签类型（NO_LABEL、SELLER_LABEL、AMAZON_LABEL）
    */
    @TableField("label_type")
    private String labelType;
    /**
    * 包装类型（混装商品、原厂包装商品）
    */
    @TableField("pack_type")
    private String packType;
    /**
    * 发货地址
    */
    @TableField("delivery_from_address")
    private String deliveryFromAddress;
    /**
    * 配送地址
    */
    @TableField("delivery_to_address")
    private String deliveryToAddress;
    /**
    * 第三方唯一编码
    */
    @TableField("fba_shipment_id")
    private String fbaShipmentId;

    /**
     * 装箱下载状态
     */
    @TableField("is_packing_download")
    private Boolean isPackingDownload;

    public static final String CODE = "code";

    public static final String NAME = "name";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String COUNTRY_ID = "country_id";

    public static final String COUNTRY_NAME = "country_name";

    public static final String FULFILLMENT_CENTER = "fulfillment_center";

    public static final String DELIVERY_STATUS = "delivery_status";

    public static final String PLATFORM_SHIPMENT_STATUS = "platform_shipment_status";

    public static final String SHIPMENT_CREATE_TIME = "shipment_create_time";

    public static final String SHIPMENT_RECEIVE_TIME = "shipment_receive_time";

    public static final String LABEL_TYPE = "label_type";

    public static final String PACK_TYPE = "pack_type";

    public static final String DELIVERY_FROM_ADDRESS = "delivery_from_address";

    public static final String DELIVERY_TO_ADDRESS = "delivery_to_address";

    public static final String PLATFORM_CODE = "platform_code";


    @Override
    public String toString() {
        return "FbaShipmentEntity{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", shopId='" + shopId + '\'' +
                ", shopName='" + shopName + '\'' +
                ", countryId='" + countryId + '\'' +
                ", countryName='" + countryName + '\'' +
                ", fulfillmentCenter='" + fulfillmentCenter + '\'' +
                ", deliveryStatus='" + deliveryStatus + '\'' +
                ", platformShipmentStatus='" + platformShipmentStatus + '\'' +
                ", shipmentCreateTime=" + shipmentCreateTime +
                ", shipmentReceiveTime=" + shipmentReceiveTime +
                ", labelType='" + labelType + '\'' +
                ", packType='" + packType + '\'' +
                ", deliveryFromAddress='" + deliveryFromAddress + '\'' +
                ", deliveryToAddress='" + deliveryToAddress + '\'' +
                '}';
    }
}