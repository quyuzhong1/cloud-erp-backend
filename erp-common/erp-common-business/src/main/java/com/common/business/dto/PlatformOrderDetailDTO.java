package com.common.business.dto;

import com.common.business.enums.BillApproveStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;

/**
 * 订单DTO 所有平台(订单明细)通用数据，转换为此类后发送mq统一消费处理
 *
 * @Author Jim
 **/
@Data
@NoArgsConstructor
public class PlatformOrderDetailDTO {

    /**
     * 图片URL
     */
    private String imageUrl;
    /**
     * skuId
     */
    private String skuId;
    /**
     * 产品sku编号
     */
    private String skuNo;

    /**
     * 平台sku编号
     */
    private String platformSkuNo;
    /**
     * 平台skuId
     */
    private String platformSkuId;

    /**
     * 平台产品id
     */
    private String platformSpuNo;
    /**
     * 库存sku编号
     */
    private String warehouseSkuNo;
    /**
     * 数量
     */
    private Integer qty;
    /**
     * 仓库id
     */
    private String warehouseId;
    /**
     * 仓库名称
     */
    private String warehouseName;
    /**
     * 单价
     */
    private BigDecimal price;
    /**
     * 金额
     */
    private BigDecimal amount;
    /**
     * 币别（原币）
     */
    private String currency;
    /**
     * 汇率
     */
    private BigDecimal exchangeRate;
    /**
     * 建议售价（本位币）
     */
    private BigDecimal advicePrice;
    /**
     * 含税成本（本位币）
     */
    private BigDecimal taxCost;
    /**
     * 来源明细id
     */
    private String sourceDetailId;
    /**
     * 标签json
     */
    private String labelJson;
    /**
     * 库存组织id
     */
    private String warehouseOrgId;
    /**
     * 库存组织名称
     */
    private String warehouseOrgName;
    /**
     * 库位
     */
    private String warehouseLocation;
    /**
     * 平台明细行号
     */
    private String platformLineNumber;
    /**
     * 平台包裹号
     */
    private String platformPackageId;
    /**
     *来源平台
     */
    private String sourcePlatform = "thirdPlatform";
    /**
     * 明细是否退款
     */
    private Boolean isDetailRefund = false;
}
