package com.sdk.oms.shopee.dto;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cInvalidTypeEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.sdk.oms.shopee.dto.order.response.OrderDetail;
import com.sdk.oms.shopee.dto.order.response.OrderItemDetail;
import com.sdk.oms.shopee.dto.order.response.Package;
import com.sdk.oms.shopee.dto.order.response.RecipientAddress;
import com.sdk.oms.shopee.dto.product.response.ImageInfo;
import com.sdk.oms.shopee.enums.OrderStatusEnum;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
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
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlatformShopeeOrderDTO extends CleanBaseDTO {

    private OrderDetail orderDetail;

    @Panno(findType = PannoEnum.EQ,field = "shopId")
    private String shopId;

    public PlatformShopeeOrderDTO(OrderDetail orderDetail, JobTaskDTO dto) {
        this.orderDetail = orderDetail;
        this.shopId = dto.getShopId();
        this.setIsClean(0);
        this.setPlatform(PlatformDictEnum.SHOPEE.getCode());
        this.setUniqueId(orderDetail.getOrdersn() + "_" + dto.getShopId());
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformOrderDTO convertDTO(PlatformShopeeOrderDTO dto) {
        PlatformOrderDTO orderDTO = new PlatformOrderDTO();
        OrderDetail orderDetail = dto.getOrderDetail();
        String shopId = dto.getShopId();

        Long createTime = orderDetail.getCreateTime();
        Instant instant = null;
        if(Objects.nonNull(createTime) && createTime.compareTo(0L) > 0){
            instant = Instant.ofEpochSecond(createTime);
        }
        ZoneId zone = ZoneId.systemDefault();

        // 订单日期
        if (Objects.nonNull(instant)){
            LocalDateTime platformCreateTime = LocalDateTime.ofInstant(instant, zone);
            orderDTO.setBillDate(platformCreateTime.toLocalDate());
            // 平台订单创建时间
            orderDTO.setPlatformOrderCreateTime(platformCreateTime);
        }
        // 平台订单号
        orderDTO.setPlatformCode(orderDetail.getOrdersn());
        // 销售平台
        orderDTO.setDictPlatform(PlatformDictEnum.SHOPEE.getCode());
        // 店铺ID
        orderDTO.setShopId(shopId);

        // 作废类型（manual手动作废，automatic自动作废）
        orderDTO.setInvalidType("");
        // 作废原因
        orderDTO.setInvalidRemark("");
        // 作废状态（false未作废，true已作废）
        orderDTO.setInvalidStatus(false);
        // 平台取消
        boolean isCancel = Boolean.FALSE;
        // 订单状态
        // （soB2cBillStatus字典类型）  SoB2cBillStatusEnum
        //UNPAID/READY_TO_SHIP/PROCESSED/SHIPPED/COMPLETED/IN_CANCEL/CANCELLED/INVOICE_PENDING
//        orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
        String orderStatus = orderDetail.getOrderStatus();
        if (OrderStatusEnum.UNPAID.getCode().equals(orderStatus)) {
            orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
            orderDTO.setApproveStatusStr(ApproveStatusEnum.WAIT_SUBMIT.getCode());
            orderDTO.setInvalidStatus(false);
        } else if (OrderStatusEnum.READY_TO_SHIP.getCode().equals(orderStatus)){
            orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
            orderDTO.setApproveStatusStr(ApproveStatusEnum.WAIT_SUBMIT.getCode());
            orderDTO.setInvalidStatus(false);
        } else if (OrderStatusEnum.PROCESSED.getCode().equals(orderStatus) || OrderStatusEnum.RETRY_SHIP.getCode().equals(orderStatus)) {
            orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
            orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE.getCode());
            orderDTO.setInvalidStatus(false);
        } else if (OrderStatusEnum.SHIPPED.getCode().equals(orderStatus) || OrderStatusEnum.TO_CONFIRM_RECEIVE.getCode().equals(orderStatus)) {
            //已完成之前 全为待发货
            orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE.getCode());
            orderDTO.setInvalidStatus(false);
        }  else if (OrderStatusEnum.IN_CANCEL.getCode().equals(orderStatus)) {
            orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_FROZEN.getCode());
            orderDTO.setApproveStatusStr(ApproveStatusEnum.REJECT.getCode());
            orderDTO.setInvalidStatus(false);
            orderDTO.setInvalidRemark("订单取消中");
            orderDTO.setInvalidType(SoB2cInvalidTypeEnum.ENUM_MANUAL.getCode());
            isCancel = Boolean.TRUE;
        } else if (OrderStatusEnum.CANCELLED.getCode().equals(orderStatus)) {
            // 作废状态（false未作废，true已作废）
            orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
            orderDTO.setApproveStatusStr(ApproveStatusEnum.WAIT_SUBMIT.getCode());
            orderDTO.setInvalidStatus(true);
            orderDTO.setInvalidRemark("订单已取消");
            orderDTO.setInvalidType(SoB2cInvalidTypeEnum.ENUM_MANUAL.getCode());
            isCancel = Boolean.TRUE;
        } else if (OrderStatusEnum.INVOICE_PENDING.getCode().equals(orderStatus)) {
            orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE.getCode());
            orderDTO.setInvalidStatus(false);
        }else if (OrderStatusEnum.TO_RETURN.getCode().equals(orderStatus) || OrderStatusEnum.COMPLETED.getCode().equals(orderStatus)) {
            orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE.getCode());
            orderDTO.setInvalidStatus(false);
        }

        // 平台订单原始状态
        orderDTO.setPlatformOrderStatus(orderStatus);
        // 订单状态
        orderDTO.setIsCancel(isCancel);

        // 付款状态（待付款、已付款）
        // （soB2cPayStatus字典类型）
        Long paytime = orderDetail.getPayTime();
        if (Objects.nonNull(paytime) && paytime.compareTo(0L) > 0) {
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
        orderDTO.setShippingFee(BigDecimal.valueOf(orderDetail.getReverseShippingFee()));
        // 付款金额
        orderDTO.setPayAmount(BigDecimal.valueOf(orderDetail.getTotalAmount()));
        // 付款方式
        orderDTO.setDictPayMethod(orderDetail.getPaymentMethod());
        // 买家备注
        orderDTO.setBuyerRemark(orderDetail.getNote());
        // 订单备注
        orderDTO.setRemark(orderDetail.getMessageToSeller());
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
        orderDTO.setInterceptRemark(orderDetail.getBuyerCancelReason());
        // 来源类型
        orderDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());
        // 来源id
        orderDTO.setSourceId(orderDetail.getOrdersn());
        // 来源编码
        orderDTO.setSourceCode(orderDetail.getOrdersn());
//        // 标签json
//        orderDTO.setLabelJson("{}");
        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");
        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");
        //明细
        orderDTO.setDetails(parseDetailDto(orderDetail));
        //B2C销售订单买家信息表
        orderDTO.setReceiver(parseReceiver(orderDetail));
        //B2C销售订单物流信息表
        List<PlatformOrderLogisticsDTO> platformOrderLogisticsDTOS = parseLogisticsList(orderDetail);
        orderDTO.setLogisticsList(platformOrderLogisticsDTOS);
        // 标签json
        if (CollectionUtils.isNotEmpty(orderDetail.getPackageList())){
            List<String> collect = orderDetail.getPackageList().stream().map(Package::getPackageNumber).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("package_number", String.join(",", collect));
            orderDTO.setLabelJson(jsonObject.toJSONString());
        }else {
            orderDTO.setLabelJson("{}");
        }
        //B2C销售订单财务信息表
        orderDTO.setFinances(parseFinances(orderDetail));
        orderDTO.setPlatform(PlatformDictEnum.SHOPEE.getCode());
        orderDTO.setUniqueId(dto.getUniqueId());
        return orderDTO;
    }

    /**
     * 批量转换明细
     */
    public static List<PlatformOrderDetailDTO> parseDetailDto(OrderDetail orderDetail) {
        if (Objects.isNull(orderDetail) || CollectionUtils.isEmpty(orderDetail.getItemList())) {
            return Collections.emptyList();
        }
        return orderDetail.getItemList().stream()
                .map(e -> intPlatformOrderDetailDTO(e, orderDetail))
                .collect(Collectors.toList());
    }

    public static PlatformOrderReceiverDTO parseReceiver(OrderDetail orderDetail) {
        if (Objects.isNull(orderDetail) || Objects.isNull(orderDetail.getRecipientAddress())) {
            return null;
        }
        RecipientAddress recipientAddress = orderDetail.getRecipientAddress();

       return PlatformOrderReceiverDTO.builder()
                .loginId(String.valueOf(orderDetail.getBuyerUserId()))
                .customerId(String.valueOf(orderDetail.getBuyerUserId()))
                .name(recipientAddress.getName())
                .receiverName(recipientAddress.getName())
                .telNumber(recipientAddress.getPhone())
                .receiverTelNumber(recipientAddress.getPhone())
                .email("")
                .country(recipientAddress.getRegion())
                .provinceName(recipientAddress.getState())
                .cityName(recipientAddress.getCity())
                .districtName(recipientAddress.getDistrict() + recipientAddress.getTown())
                .postCode(recipientAddress.getZipcode())
                .firstAddress(recipientAddress.getFullAddress())
                .fullAddress(recipientAddress.getFullAddress())
                .build();
    }

    public static List<PlatformOrderLogisticsDTO> parseLogisticsList(OrderDetail orderDetail) {
        if (Objects.isNull(orderDetail) || CollectionUtils.isEmpty(orderDetail.getPackageList())) {
            return Collections.emptyList();
        }
        List<PlatformOrderLogisticsDTO> logisticsDTOS = new ArrayList<>();
        List<Package> packages = orderDetail.getPackageList();
        packages.forEach(p -> {
            Long shipByDate = orderDetail.getShipByDate();
            LocalDateTime deliveryTime = null;
            if (Objects.nonNull(shipByDate) && shipByDate.compareTo(0L) > 0){
                Instant instant = Instant.ofEpochSecond(shipByDate);
                ZoneId zone = ZoneId.systemDefault();
                deliveryTime = LocalDateTime.ofInstant(instant, zone);
            }
            PlatformOrderLogisticsDTO dto = PlatformOrderLogisticsDTO.builder()
                    .code(p.getPackageNumber())
                    .name(LogisticsPlatformEnum.SHOPEE.getName())
                    .deliveryTime(deliveryTime)
                    .logisticsChannelName(p.getShippingCarrier())
                    .estimatedShippingCost(BigDecimal.valueOf(orderDetail.getEstimatedShippingFee()))
                    .actualShippingCost(BigDecimal.valueOf(orderDetail.getActualShippingFee()))
                    .accessoriesCostCurrency(orderDetail.getCurrency())
                    .actualShippingCurrency(orderDetail.getCurrency())
                    .estimatedShippingCurrency(orderDetail.getCurrency())
                    .build();
            logisticsDTOS.add(dto);
        });
        return logisticsDTOS;
    }

    public static PlatformOrderFinanceDTO parseFinances(OrderDetail orderDetail) {
        PlatformOrderFinanceDTO dto = PlatformOrderFinanceDTO.builder()
                .currency(orderDetail.getCurrency())
                .shippingCost(BigDecimal.valueOf(orderDetail.getReverseShippingFee()))
                .logisticsCost(BigDecimal.valueOf(orderDetail.getActualShippingFee()))
                .build();
        return dto;
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
        detailDTO.setSkuId("");
        // skuNo
        detailDTO.setSkuNo("");

        // 平台sku编号
        detailDTO.setPlatformSkuNo(StringUtils.isNotEmpty(item.getItemSku()) ? item.getItemSku() : item.getModelSku());

        // 平台产品id
        detailDTO.setPlatformSpuNo(Objects.nonNull(item.getItemId()) ? item.getItemId().toString() : "");

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
            BigDecimal amount = BigDecimal.valueOf(item.getModelOriginalPrice()).multiply(BigDecimal.valueOf(item.getModelQuantityPurchased()));
            detailDTO.setAmount(amount);
        } catch (Exception e) {
            log.error("计算金额异常：打折后金额：{},数量{}", item.getModelOriginalPrice(), item.getModelQuantityPurchased());
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
        detailDTO.setSourceDetailId(item.getItemId()+"_"+item.getModelId());
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

    @Override
    public String toString() {
        return "PlatformShopeeOrderDTO{" +
                "orderDetail=" + orderDetail +
                ", shopId='" + shopId + '\'' +
                '}';
    }
}
