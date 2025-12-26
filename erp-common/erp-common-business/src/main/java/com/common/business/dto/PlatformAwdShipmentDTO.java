package com.common.business.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * FBA货件DTO 所有平台订单通用数据，转换为此类后发送mq统一消费处理
 *
 * @author Jim
 * @date 2023/11/1
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
public class PlatformAwdShipmentDTO extends UniqueDto {
    /**
     * FBA货件名称
     */
    private String name;
    /**
     * 店铺id
     */
    private String shopId;
    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 国家二字码
     */
    private String countryId;
    /**
     * 国家名称
     */
    private String countryName;
    /**
     * 平台物流中心
     */
    private String fulfillmentCenter;
    /**
     * 发货状态
     */
    private String deliveryStatus;
    /**
     * 平台货件状态
     */
    private String platformShipmentStatus;
    /**
     * 创建时间（拉取数据的日期）
     */
    private LocalDateTime shipmentCreateTime;
    /**
     * 签收时间（拉取签收数据的日期）
     */
    private LocalDateTime shipmentReceiveTime;
    /**
     * 标签类型（NO_LABEL、SELLER_LABEL、AMAZON_LABEL）
     */
    private String labelType;
    /**
     * 包装类型（混装商品、原厂包装商品）
     */
    private String packType;
    /**
     * 发货地址
     */
    private String deliveryFromAddress;

    /**
     * 第三方唯一编码
     */
    private String fbaShipmentId;
    /**
     * 平台最后修改时间
     */
    private LocalDateTime platformUpdateTime;

    /**
     * 订单类型
     * FbaOutStockTypeEnum
     */
    private String orderType;
    /**
     * 面单url
     */
    private String labelUrl;
    /**
     * 面单类型
     */
    private String pageType;

    /**
     * 入库计划单号
     */
    private String planCode;
    /**
     * 发货单号
     */
    private String deliveryCode;
    /**
     * 发货单ID
     */
    private String deliveryId;
    /**
     * 地区偏好
     */
    private String preferredRegion;
    /**
     * 发货人
     */
    private String deliveryFromName;
    /**
     * 发货手机号
     */
    private String deliveryFromMobile;
    /**
     * 发货城市
     */
    private String deliveryFromCity;
    /**
     * 发货州/省
     */
    private String deliveryFromProvince;
    /**
     * 发货地区
     */
    private String deliveryFromArea;
    /**
     * 发货邮编
     */
    private String deliveryFromPostCode;
    /**
     * 平台货件发货时间（拉取数据的日期）
     */
    private LocalDateTime shipmentDeliveryTime;
    /**
     * 收货电话号码
     */
    private String deliveryToMobile;
    /**
     * 收货人
     */
    private String deliveryToName;
    /**
     * 收货邮编
     */
    private String deliveryToPostCode;
    /**
     * 收货地区
     */
    private String deliveryToArea;
    /**
     * 收货州/省
     */
    private String deliveryToProvince;
    /**
     * 收货城市
     */
    private String deliveryToCity;
    /**
     * 收货国家
     */
    private String deliveryToCountryId;
    /**
     * 收货国家名称
     */
    private String deliveryToCountryName;
    /**
     * 详细地址
     */
    private String deliveryToAddress;

    /**
     * 货件详情(由物流签收信息合并）
     */
    List<PlatformAwdShipmentReceiveDTO> detailList;

    public List<PlatformAwdShipmentReceiveDTO> checkAndGetDetailList(){
        if (CollectionUtils.isEmpty(this.detailList)){
            return Collections.emptyList();
        }
        return this.detailList;
    }
}
