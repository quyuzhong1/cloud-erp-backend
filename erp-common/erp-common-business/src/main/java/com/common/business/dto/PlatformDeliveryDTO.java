package com.common.business.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单DTO 发货单列表
 *
 * @Author zdy
 **/
@Data
@NoArgsConstructor
public class PlatformDeliveryDTO {

    /**
     * erp 主记录id
     */
    private String mainId;
    /**
     * 扩展字段
     */
    private String extendFields;
    /**
     * 来源单号（履约单号）
     */
    private String sourceCode;
    /**
     * 物流单号
     */
    private String trackNo;
    /**
     * 运单号
     */
    private String transportNo;

    /**
     * 订单状态
     */
    private String orderStatus;
    /**
     * 包裹费用
     */
    private String packageAmount;
    /**
     * 收件国家
     */
    private String receiverCountry;
    /**
     * 收件人电话
     */
    private String receiverMobile;
    /**
     * 收件人姓名
     */
    private String receiverName;
    /**
     * 收件人电话前缀
     */
    private String receiverPhone;
    /**
     * 下发到仓时间戳
     */
    private String deliveryWarehouseTime;
    /**
     * 订单创建时间
     */
    private String createTime;

    /**
     * 平台仓库名称
     */
    private String platformWarehouseName;

    /**
     * ERP仓库组织id
     */
    private String warehouseOrgId;
    /**
     * ERP仓库组织名称
     */
    private String warehouseOrgName;
    /**
     *来源平台
     */
    private String sourcePlatform = "thirdPlatform";
    /**
     * 发货单明细
     */
    private List<PlatformDeliveryDetailDTO> detailDTOList;

}
