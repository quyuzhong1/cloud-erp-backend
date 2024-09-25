package com.erp.model.dmp.entity;

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
 * FBA货件表
 * </p>
 *
 * @author Jim
 * @since 2024-09-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_fba_shipment")
public class DmpFbaShipmentEntity extends BaseEntity<DmpFbaShipmentEntity> {

    /**
     * FBA货件名称
     */
    @TableField("name")
    private String name;
    /**
     * 国家二字码
     */
    @TableField("country_id")
    private String countryId;
    /**
     * 平台物流中心
     */
    @TableField("fulfillment_center")
    private String fulfillmentCenter;
    /**
     * 平台货件状态
     */
    @TableField("platform_shipment_status")
    private String platformShipmentStatus;
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
     * 输入任务ID
     */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
     * 亚马逊账号代号
     */
    @TableField("platform_shop_code")
    private String platformShopCode;
    /**
     * 任务转换ID
     */
    @TableField("convert_id")
    private String convertId;
    /**
     * 店铺ID
     */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
     * 任务来源唯一加密代号
     */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
     * 任务数据加密代号
     */
    @TableField("data_encrypt")
    private String dataEncrypt;


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

    public static final String FBA_SHIPMENT_ID = "fba_shipment_id";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String PLATFORM_SHOP_CODE = "platform_shop_code";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

}