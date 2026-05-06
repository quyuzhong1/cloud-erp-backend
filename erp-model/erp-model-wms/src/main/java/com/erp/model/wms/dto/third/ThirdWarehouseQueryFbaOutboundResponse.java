package com.erp.model.wms.dto.third;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ThirdWarehouseQueryFbaOutboundResponse extends ThirdWarehouseAuth{

    /**
     * 订单类型，B2B/B2C
     */
    private String orderType;

    /**
     * 订单号，根据订单匹配B2B三方发货单更新
     */
    private String platformOrderCode;
    /**
     * 发货单号
     */
    private String code;
    /**
     * 物流跟踪号
     */
    private String trackNo;
    /**
     *
     * 异常类型，需要记录到B2B三方发货单的异常原因
     * SKU_NOT_STOCK：SKU无货
     * CHANNEL_NOT_SUPPORT：渠道不支持
     * CUSTOMER_CANCEL：客户取消订单
     */
    private String errorType;
    /**
     * 发货时间
     * 2023-05-29 16:48:07
     */
    private String deliveryTimeStr;

    /**
     * 平台创建时间
     */
    private String platformCreateTimeStr;

    /**
     * 平台修改时间
     */
    private String platformUpdateTimeStr;
    /**
     * ERP标准状态，供WMS内部业务流转使用
     */
    private String status;

    /**
     * 海外仓原始状态，供DMP落原始状态使用
     */
    private String platformOriginalStatus;

    /**
     * zhongbao 海外仓出库异常时，记录异常原型到操作日志
     */
    private String errorReason;

    /**
     * 平台：zhongbao用作判断订单状态是否需要解析数据
     */
    private String platform;

    /**
     * 平台订单号
     */
    private String swOrderNumber;

    /**
     * 仓库代码
     */
    private String warehouseCode;

    /**
     * 运输方式
     */
    private String shippingMethod;

    /**
     * 承运商
     */
    private String carrierName;

    /**
     * 拦截状态
     */
    private String interceptStatus;

}
