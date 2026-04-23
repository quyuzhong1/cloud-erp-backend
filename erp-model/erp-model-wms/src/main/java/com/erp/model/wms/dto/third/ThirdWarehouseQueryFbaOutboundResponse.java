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
     * 以下状态B2B三方发货单状态不作变更
     * NEW：草稿
     * SUBMIT：已提交
     * PROCESSED：出库中
     * WAIT_UPLOAD：待上传
     * UPLOADED：已上传
     * BLOCK：订单拦截中
     * DISCARD_PROCESSED：作废中
     *
     * 以下状态自动变更为取消发货，有拦截标识时清空拦截标识，记录拦截成功
     * EXCEPTION：出库异常
     * DISCARD：已作废
     * PROBLEM：问题件
     *
     * 以下状态自动变更为已发货，并自动生成销售出库单自动审核
     * SUCCESS：已出库
     */
    private String status;

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
