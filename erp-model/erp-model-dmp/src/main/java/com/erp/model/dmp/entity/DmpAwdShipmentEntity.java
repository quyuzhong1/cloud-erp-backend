package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * FBA货件表
 * </p>
 *
 * @author zdy
 * @since 2025-12-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("dmp_awd_shipment")
public class DmpAwdShipmentEntity extends BaseEntity<DmpAwdShipmentEntity> {

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
    /**
    * 关联单号
    */
    @TableField("reference_id")
    private String referenceId;

    /**
     * 平台创建时间
     */
    @TableField("platform_create_time")
    private LocalDateTime platformCreateTime;
    /**
     * 平台更新时间
     */
    @TableField("platform_update_time")
    private LocalDateTime platformUpdateTime;
    /**
     * 平台订单ID
     */
    @TableField("platform_order_id")
    private String platformOrderId;

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
    /**
     * 面单url
     */
    @TableField("label_url")
    private String labelUrl;
    /**
     * 面单类型
     */
    @TableField("page_type")
    private String pageType;

    public static final String NAME = "name";

    public static final String COUNTRY_ID = "country_id";

    public static final String FULFILLMENT_CENTER = "fulfillment_center";

    public static final String PLATFORM_SHIPMENT_STATUS = "platform_shipment_status";

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

    public static final String REFERENCE_ID = "reference_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}