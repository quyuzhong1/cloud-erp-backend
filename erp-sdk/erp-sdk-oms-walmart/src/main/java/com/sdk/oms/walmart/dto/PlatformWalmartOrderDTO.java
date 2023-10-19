package com.sdk.oms.walmart.dto;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.*;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.sdk.oms.walmart.dto.walmart.item.ItemResponseBean;
import com.sdk.oms.walmart.dto.walmart.order.ItemBean;
import com.sdk.oms.walmart.dto.walmart.order.OrderBean;
import com.sdk.oms.walmart.dto.walmart.order.OrderLineBean;
import com.sdk.oms.walmart.dto.walmart.order.OrderLineStatusBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformWalmartOrderDTO extends CleanBaseDTO {

    private OrderBean orderBean;

    /**
     * 初始化
     */
    public PlatformWalmartOrderDTO(OrderBean orderBean, JobTaskDTO dto) {
        this.orderBean = orderBean;
        this.setIsClean(0);
        this.setPlatform(PlatformDictEnum.SHOPIFY.getCode());
        this.setUniqueId(orderBean.getCustomerOrderId());
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformOrderDTO convertDTO(PlatformWalmartOrderDTO dto) {
        // 原商品信息
        return initPlatformProductDTO(dto);
    }

    /**
     * 根据PlatformWalmartListingDTO 转换 DTO
     */
    private static PlatformOrderDTO initPlatformProductDTO(PlatformWalmartOrderDTO dto) {
        //获取原始订单信息
        OrderBean orderBean = dto.getOrderBean();

        //设置对应关系
        PlatformOrderDTO orderDTO = new PlatformOrderDTO();

        //平台订单号
        orderDTO.setPlatformCode(orderBean.getPurchaseOrderId());

        //销售平台
        orderDTO.setDictPlatform(PlatformDictEnum.WALMART.getCode());

        //付款时间
        Instant instant = Instant.ofEpochMilli(orderBean.getOrderDate());
        ZoneId zone = ZoneId.systemDefault();
        orderDTO.setPayTime(LocalDateTime.ofInstant(instant, zone));

        // 订单状态，详情金额汇总
        fieldHandler(orderBean.getOrderLines().getOrderLine(), orderDTO);

        return orderDTO;
    }

    /**
     * 批量转换明细
     */
    public static List<PlatformOrderDetailDTO> parseDetailDto(OrderBean orderBean) {
        return orderBean.getOrderLines().getOrderLine().stream()
                .map(e -> intPlatformOrderDetailDTO(e, orderBean))
                .collect(Collectors.toList());
    }


    /**
     * 转换明细
     */
    private static PlatformOrderDetailDTO intPlatformOrderDetailDTO(OrderLineBean orderLineBean, OrderBean orderBean) {
        PlatformOrderDetailDTO detailDTO = new PlatformOrderDetailDTO();
        ItemBean item = orderLineBean.getItem();
        // 图片URL
        detailDTO.setImageUrl(item.getImageUrl());
        // skuId
        detailDTO.setSkuId("");
        // skuNo
        detailDTO.setSkuNo("");
        // 卖家sku编号
        detailDTO.setSellerSkuNo("");
        // 平台sku编号
        detailDTO.setPlatformSkuNo(item.getSku());
        // 库存sku编号
        detailDTO.setWarehouseName("");
        // 仓库名称
        // 库存是否扣除
        detailDTO.setWarehouseId("");
        // 数量
        detailDTO.setQty(orderLineBean.getOrderLineQuantity().getAmount());
        // 单价
        detailDTO.setPrice(BigDecimal.ZERO);
        // 金额
        BigDecimal amount = orderLineBean.getCharges().getCharge().stream().filter(req -> "PRODUCT".equals(req.getChargeType()))
                .map(req -> req.getChargeAmount().getAmount())
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        detailDTO.setAmount(amount);
        // 币别（原币）
        String currency = orderLineBean.getCharges().getCharge().stream().map(req -> req.getChargeAmount().getCurrency()).findFirst().orElse("");
        detailDTO.setCurrency(currency);
        // 汇率
        detailDTO.setExchangeRate(BigDecimal.ONE);
        // 建议售价（本位币）
        detailDTO.setAdvicePrice(BigDecimal.ZERO);
        // 含税成本（本位币）
        detailDTO.setTaxCost(BigDecimal.ZERO);
        // 来源明细id
        detailDTO.setSourceDetailId("");
        // 标签json
        detailDTO.setLabelJson("");
        // 库存组织id
        detailDTO.setWarehouseOrgId("");
        // 库存组织名称
        detailDTO.setWarehouseOrgName("");
        // 库位
        detailDTO.setWarehouseLocation("");
        return detailDTO;
    }

    /**
     * 字段处理：状态，金额
     * 沃尔玛订单行的状态。有效状态为：Created（已创建）、Acknowledged（已确认）、Shipped（已发货）、Delivered（已交付）和 Cancelled(已取消)。
     * Status of purchase order line. Valid statuses are: Created, Acknowledged, Shipped, Delivered and Cancelled.
     * @param orderLineList 详情信息
     * @param orderDTO 设置的字段类型
     * @return java.lang.String
     **/
    private static void fieldHandler(List<OrderLineBean> orderLineList, PlatformOrderDTO orderDTO) {
        //沃尔玛订单行的状态。有效状态为：Created（已创建）、Acknowledged（已确认）、Shipped（已发货）、Delivered（已交付）和 Cancelled(已取消)。
        //Status of purchase order line. Valid statuses are: Created, Acknowledged, Shipped, Delivered and Cancelled.
        //设置状态
        List<OrderLineStatusBean> statusList = orderLineList.stream().map(req -> req.getOrderLineStatuses().getOrderLineStatus().get(0)).collect(Collectors.toList());
        //沃尔玛：已创建
        List<OrderLineStatusBean> created = statusList.stream().filter(req -> req.getStatus().contains("Created")).collect(Collectors.toList());
        //沃尔玛：已确认
        List<OrderLineStatusBean> inDistribution = statusList.stream().filter(req -> req.getStatus().contains("Acknowledged")).collect(Collectors.toList());
        //沃尔玛：已发货
        List<OrderLineStatusBean> shipped = statusList.stream().filter(req -> req.getStatus().contains("Shipped")).collect(Collectors.toList());
        //沃尔玛：已交付
        List<OrderLineStatusBean> delivered = statusList.stream().filter(req -> req.getStatus().contains("Delivered")).collect(Collectors.toList());
        //沃尔玛：已取消
        List<OrderLineStatusBean> cancelled = statusList.stream().filter(req -> req.getStatus().contains("Cancelled")).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(created)) {
            //沃尔玛：已创建 = OMS：待配货
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
        } else if (CollectionUtils.isEmpty(inDistribution)) {
            //沃尔玛：已确认 = OMS：待发货
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
        } else if (CollectionUtils.isEmpty(shipped)) {
            //沃尔玛：已发货 = OMS：已发货
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
        } else if (CollectionUtils.isEmpty(delivered)) {
            //沃尔玛：已交付 = OMS：已发货
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
        } else if (CollectionUtils.isEmpty(cancelled)) {
            //沃尔玛：已取消 = OMS：已作废
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_INVALID.getCode());
        }

        BigDecimal amount = new BigDecimal(BigInteger.ZERO);
        String currency = "";
        for (OrderLineBean orderLineBean : orderLineList) {
            //设置订单金额
            amount = amount.add(orderLineBean.getCharges().getCharge().stream().filter(req -> "PRODUCT".equals(req.getChargeType()))
                    .map(req -> req.getChargeAmount().getAmount())
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO));
            //设置币别
            currency = orderLineBean.getCharges().getCharge().stream().map(req -> req.getChargeAmount().getCurrency()).findFirst().orElse("");
            orderDTO.setCurrency(currency);
        }
        orderDTO.setAmount(amount);
    }

}
