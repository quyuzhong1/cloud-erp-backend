package com.sdk.oms.shopee.dto;

import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderDetailDTO;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.sdk.oms.shopee.dto.order.response.OrderDetail;
import com.sdk.oms.shopee.dto.order.response.OrderItemDetail;
import com.sdk.oms.shopee.dto.product.response.ImageInfo;
import com.sdk.oms.shopee.enums.OrderStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 平台亚马逊订单DTO
 *
 * @Author Cloud
 * @Date 2023/8/31 16:01
 **/
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformShopeeOrderDTO extends CleanBaseDTO {

    private OrderDetail orderDetail;

    public PlatformShopeeOrderDTO(OrderDetail orderDetail, JobTaskDTO dto) {
        this.orderDetail = orderDetail;
        this.setIsClean(0);
        this.setPlatform(PlatformDictEnum.SHOPEE.getCode());
        this.setUniqueId(orderDetail.getOrdersn());
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformOrderDTO convertDTO(PlatformShopeeOrderDTO dto) {
        PlatformOrderDTO orderDTO = new PlatformOrderDTO();
        OrderDetail orderDetail = dto.getOrderDetail();

        Long createTime = orderDetail.getCreateTime();
        Instant instant = Instant.ofEpochSecond(createTime);
        ZoneId zone = ZoneId.systemDefault();

        // 订单日期
        orderDTO.setBillDate(LocalDateTime.ofInstant(instant, zone).toLocalDate());
        // 平台订单号
        orderDTO.setPlatformCode(orderDetail.getOrdersn());
        // 销售平台
        orderDTO.setDictPlatform(PlatformDictEnum.SHOPEE.getCode());
        // 店铺ID
        orderDTO.setShopId(null);

        // 作废类型（manual手动作废，automatic自动作废）
        orderDTO.setInvalidType("");
        // 作废原因
        orderDTO.setInvalidRemark("");
        // 作废状态（false未作废，true已作废）
        orderDTO.setInvalidStatus(false);
        // 订单状态
        // （soB2cBillStatus字典类型）  SoB2cBillStatusEnum
        //UNPAID/READY_TO_SHIP/PROCESSED/SHIPPED/COMPLETED/IN_CANCEL/CANCELLED/INVOICE_PENDING
        String orderStatus = orderDetail.getStatus();
        if (OrderStatusEnum.UNPAID.getCode().equals(orderStatus)) {
            orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
        } else if (OrderStatusEnum.READY_TO_SHIP.getCode().equals(orderStatus)) {
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode());
        } else if (OrderStatusEnum.PROCESSED.getCode().equals(orderStatus)) {
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
        } else if (OrderStatusEnum.SHIPPED.getCode().equals(orderStatus)) {
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
        } else if (OrderStatusEnum.COMPLETED.getCode().equals(orderStatus)) {
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
        } else if (OrderStatusEnum.IN_CANCEL.getCode().equals(orderStatus)) {
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_FROZEN.getCode());
        } else if (OrderStatusEnum.CANCELLED.getCode().equals(orderStatus)) {
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_INVALID.getCode());
            // 作废状态（false未作废，true已作废）
            orderDTO.setInvalidStatus(true);
        } else if (OrderStatusEnum.INVOICE_PENDING.getCode().equals(orderStatus)) {
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
        }

        // 付款状态（待付款、已付款）
        // （soB2cPayStatus字典类型）
        Long paytime = orderDetail.getPayTime();
        if (Objects.isNull(paytime)) {
            orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
        } else {
            orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode());
            Instant instant2 = Instant.ofEpochSecond(paytime);
            // 付款时间
            orderDTO.setPayTime(LocalDateTime.ofInstant(instant2, zone));
        }
        // 订单金额
        orderDTO.setAmount(BigDecimal.valueOf(orderDetail.getTotalAmount()));
        // 币别（原币）
        orderDTO.setCurrency(orderDetail.getCurrency());
        // 汇率
        orderDTO.setExchangeRate(BigDecimal.ONE);
        // 运费收入
        orderDTO.setShippingFee(BigDecimal.valueOf(orderDetail.getActualShippingFee()));
        // 付款金额
        orderDTO.setPayAmount(null);
        // 付款方式
        orderDTO.setDictPayMethod(orderDetail.getPaymentMethod());
        // 买家备注
        orderDTO.setBuyerRemark(orderDetail.getNote());
        // 订单备注
        orderDTO.setRemark(null);
        // 销售组织id
        Long buyerUserId = orderDetail.getBuyerUserId();
        if (Objects.nonNull(buyerUserId)) {
            orderDTO.setOrgId(String.valueOf(buyerUserId));
        }
        // 销售组织名称
        orderDTO.setOrgName(orderDetail.getBuyerUsername());
        // 是否拦截
        orderDTO.setIsIntercept(false);
        // 拦截备注
        orderDTO.setInterceptRemark("");
        // 来源类型
        orderDTO.setSourceType("soB2c");
        // 来源id
        orderDTO.setSourceId(orderDetail.getOrdersn());
        // 来源编码
        orderDTO.setSourceCode(orderDetail.getOrdersn());
        // 标签json
        orderDTO.setLabelJson(null);
        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");
        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");
        //明细
        orderDTO.setDetails(parseDetailDto(orderDetail));

        return new PlatformOrderDTO();
    }

    /**
     * 批量转换明细
     */
    public static List<PlatformOrderDetailDTO> parseDetailDto(OrderDetail orderDetail) {
        return orderDetail.getItems().stream()
                .map(e -> intPlatformOrderDetailDTO(e, orderDetail))
                .collect(Collectors.toList());
    }

    /**
     * 转换明细
     */
    private static PlatformOrderDetailDTO intPlatformOrderDetailDTO(OrderItemDetail item, OrderDetail orderDetail) {
        PlatformOrderDetailDTO detailDTO = new PlatformOrderDetailDTO();
        // 图片URL
        ImageInfo image = item.getImageInfo();
        if (Objects.nonNull(image)) {
            detailDTO.setImageUrl(image.getImageUrl());
        }
        // skuId
        detailDTO.setSkuId(String.valueOf(item.getItemId()));
        // skuNo
        detailDTO.setSkuNo(item.getItemSku());
        // 卖家sku编号
        detailDTO.setSellerSkuNo("");
        // 平台sku编号
        detailDTO.setPlatformSkuNo(item.getModelSku());
        // 库存sku编号
        detailDTO.setWarehouseName("");
        // 仓库名称
        // 库存是否扣除
        detailDTO.setWarehouseId("");
        // 数量
        detailDTO.setQty(item.getModelQuantityPurchased());
        // 单价
        detailDTO.setPrice(BigDecimal.valueOf(item.getModelOriginalPrice()));
        // 金额
        try {
            BigDecimal amount = BigDecimal.valueOf(item.getModelDiscountedPrice()).multiply(BigDecimal.valueOf(item.getModelQuantityPurchased()));
            detailDTO.setAmount(amount);
        } catch (Exception e) {
            log.error("计算金额异常：打折后金额：{},数量{}", item.getModelDiscountedPrice(), item.getModelQuantityPurchased());
        }

        // 币别（原币）
        detailDTO.setCurrency(orderDetail.getCurrency());
        // 汇率
        detailDTO.setExchangeRate(BigDecimal.ONE);
        // 建议售价（本位币）
        detailDTO.setAdvicePrice(BigDecimal.ZERO);
        // 含税成本（本位币）
        detailDTO.setTaxCost(BigDecimal.ZERO);
        // 来源明细id
        detailDTO.setSourceDetailId(String.valueOf(item.getItemId()));
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

}
