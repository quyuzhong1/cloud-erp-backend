package com.common.business.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
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
public class PlatformFbaShipmentDTO extends UniqueDto {
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
     * 物流签收信息
     */
    List<PlatformFbaShipmentReceiveDTO> receiveDTOList;
}
