package com.common.business.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.python.antlr.ast.Str;

import java.time.LocalDateTime;

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
     * 类型 platform 平台  warehouse 仓库
     */
    private String platformType;

    /**
     * 平台sku no
     */
    private String platformSkuNo;

    /**
     * 平台sku name
     */
    private String platformSkuName;

    /**
     * 平台产品(spu) no或id
     */
    private String platformProductNo;

    /**
     * 平台产品(spu)名称
     */
    private String platformProductName;
    /**
     * 平台商品条码
     */
    private String platformProductBarcode;

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

    /**
     * 类型 platform 平台  warehouse 仓库
     */
    private String type = "platform";

    /**
     * 平台最后修改时间
     */
    private LocalDateTime platformUpdateTime;

    /**
     * 详情或其他数据下载状态
     * 0 详情数据需要更新
     * 1 详情数据已更新
     */
    private Integer downloadStatus;

    /**
     * 数据下载时间
     */
    private String downloadTime;

    /**
     * ERP系统店铺ID
     */
    private String shopId;

    /**
     * ERP系统店铺ID
     */
    private String authId;

    /**
     * 匹配结果
     */
    private Boolean matchResult;

    /**
     * 匹配结果
     */
    private String matchResultStr;

    /**
     * 亚马逊关联的SKU
     */
    private String platformFnSku;

    /**
     * 平台的Listing状态
     */
    private String platformStatus;

    /**
     * 平台的SKU id
     */
    private String platformSkuId;

    /**
     * 父平台产品ID（父ASIN）
     */
    private String platformParentSkuId;
}
