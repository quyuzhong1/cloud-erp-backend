package com.common.business.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 产品DTO 所有平台订单通用数据，转换为此类后发送mq统一消费处理
 *
 * @Author Cloud
 * @Date 2023/8/31 16:01
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
public class PlatformProductDTO extends UniqueDto {

    /**
     * sku no
     */
//    private String skuNo;

    /**
     * 产品名称
     */
//    private String productName;

    /**
     * 平台sku no
     */
    private String platformSkuNo;

    /**
     * 平台产品(spu) no或id
     */
    private String platformProductNo;

    /**
     * 平台产品名称
     */
    private String platformProductName;

    /**
     * 平台
     */
    private String platform;

    /**
     * 类型 platform 平台  warehouse 仓库
     */
    private String type;

    /**
     * 匹配结果吧true 已匹配 false 未匹配
     */
//    private Boolean matchResult;

    /**
     * 产品图片 url
     */
    private String productImageUrl;

    /**
     * 产品规格信息
     */
    private String productSpec;

    /**
     * 产品包装信息
     */
    private String productPacking;
}
