package com.sdk.oms.walmart.dto;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.*;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.sdk.oms.walmart.dto.walmart.item.ItemResponseBean;
import com.sdk.oms.walmart.dto.walmart.order.ItemBean;
import com.sdk.oms.walmart.dto.walmart.order.OrderBean;
import com.sdk.oms.walmart.dto.walmart.order.OrderLineBean;
import com.sdk.oms.walmart.dto.walmart.order.OrderLineStatusBean;
import com.sdk.oms.walmart.dto.walmart.ship.TrackingInfoBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformWalmartOrderDTO extends CleanBaseDTO {

    private OrderBean orderBean;

    private WalmartShopInfoDTO walmartShopInfoDTO;

    /**
     * 初始化
     */
    public PlatformWalmartOrderDTO(OrderBean orderBean, JobTaskDTO dto, WalmartShopInfoDTO walmartShopInfoDTO) {
        this.orderBean = orderBean;
        this.walmartShopInfoDTO = walmartShopInfoDTO;
        this.setIsClean(0);
        this.setPlatform(PlatformDictEnum.WALMART.getCode());
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

        //店铺信息
        WalmartShopInfoDTO walmartShopInfoDTO = dto.getWalmartShopInfoDTO();

        //设置对应关系
        PlatformOrderDTO orderDTO = new PlatformOrderDTO();

        //平台订单号
        orderDTO.setPlatformCode(orderBean.getPurchaseOrderId());

        //销售平台
        orderDTO.setDictPlatform(PlatformDictEnum.WALMART.getCode());

        // 店铺ID
        orderDTO.setShopId(walmartShopInfoDTO.getId());

        // 付款状态（待付款、已付款）
        orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode());

        //付款时间
        Instant instant = Instant.ofEpochMilli(orderBean.getOrderDate());
        ZoneId zone = ZoneId.systemDefault();
        orderDTO.setPayTime(LocalDateTime.ofInstant(instant, zone));

        // 订单状态，详情金额汇总
        fieldHandler(orderBean.getOrderLines().getOrderLine(), orderDTO);



        // 是否拦截
        orderDTO.setIsIntercept(false);

        // 拦截备注
        orderDTO.setInterceptRemark("");

        // 来源类型
        orderDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());

        // 来源id
        orderDTO.setSourceId(orderBean.getPurchaseOrderId());

        // 来源编码
        orderDTO.setSourceCode("");

        // 标签json
        orderDTO.setLabelJson("{}");

        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");

        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");

        // 订单明细
        List<PlatformOrderDetailDTO> details = parseDetailDto(orderBean);
        orderDTO.setDetails(details);

        //B2C销售订单买家信息表
        orderDTO.setReceiverList(parseReceiverList(orderBean));
        //B2C销售订单物流信息表
        orderDTO.setLogisticsList(parseLogisticsList(orderBean.getOrderLines().getOrderLine()));
        //B2C销售订单财务信息表
        orderDTO.setFinancesList(parseFinancesList(orderBean));
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

        // 平台sku编号
        detailDTO.setPlatformSkuNo(item.getSku());

        //平台产品id
        detailDTO.setPlatformSpuNo("");

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
        //订单金额
        orderDTO.setAmount(amount);

        // 付款金额
        orderDTO.setPayAmount(amount);
    }

    /**
     * 买家信息字段处理
     * @Author Luo_WG
     * @Date 2023/12/4 9:38
     * @param orderBean
     * @return java.util.List<com.common.business.dto.PlatformOrderReceiverDTO>
     **/
    private static List<PlatformOrderReceiverDTO> parseReceiverList(OrderBean orderBean) {
        if (Objects.isNull(orderBean) || Objects.isNull(orderBean.getShippingInfo())) {
            return Collections.emptyList();
        }
        List<PlatformOrderReceiverDTO> receiverDTOS = new ArrayList<>();
        receiverDTOS.add(PlatformOrderReceiverDTO.builder()
                .loginId("")
                .customerId("")
                .name(orderBean.getShippingInfo().getPostalAddress().getName())
                .telNumber(orderBean.getShippingInfo().getPhone())
                .receiverTelNumber(orderBean.getShippingInfo().getPhone())
                .email(orderBean.getCustomerEmailId())
                .country(orderBean.getShippingInfo().getPostalAddress().getCountry())
                .provinceName(orderBean.getShippingInfo().getPostalAddress().getState())
                .cityName(orderBean.getShippingInfo().getPostalAddress().getCity())
                .districtName(orderBean.getShippingInfo().getPostalAddress().getCountry() + " " + orderBean.getShippingInfo().getPostalAddress().getCity())
                .postCode(orderBean.getShippingInfo().getPostalAddress().getPostalCode())
                .firstAddress(orderBean.getShippingInfo().getPostalAddress().getAddress1())
                .fullAddress(orderBean.getShippingInfo().getPostalAddress().getAddress2())
                .build());
        return receiverDTOS;
    }

    /**
     * 物流信息字段处理
     * @Author Luo_WG
     * @Date 2023/12/4 14:07
     * @param orderLineBeanList
     * @return java.util.List<com.common.business.dto.PlatformOrderLogisticsDTO>
     **/
    private static List<PlatformOrderLogisticsDTO> parseLogisticsList(List<OrderLineBean> orderLineBeanList) {
        if (CollectionUtils.isEmpty(orderLineBeanList)) {
            return Collections.emptyList();
        }
        List<PlatformOrderLogisticsDTO> logisticsDTOS = new ArrayList<>();
        for (OrderLineBean orderLineBean : orderLineBeanList) {
            List<OrderLineStatusBean> orderLineStatus = orderLineBean.getOrderLineStatuses().getOrderLineStatus();
            if (CollectionUtils.isEmpty(orderLineStatus)) {
                return Collections.emptyList();
            }

            for (OrderLineStatusBean lineStatus : orderLineStatus) {
                TrackingInfoBean trackingInfo = lineStatus.getTrackingInfo();
                if (ObjectUtil.isEmpty(trackingInfo)) {
                    continue;
                }
                //chargeAmount中chargeType=SHIPPING时amount的订单行汇总
                BigDecimal cost = orderLineBean.getCharges().getCharge().stream()
                        .filter(req -> "SHIPPING".equals(req.getChargeType()))
                        .map(req -> req.getChargeAmount().getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);

                String currency = orderLineBean.getCharges().getCharge().stream()
                        .filter(req -> "SHIPPING".equals(req.getChargeType()))
                        .map(req -> req.getChargeAmount().getCurrency()).findFirst().orElse("");


                Instant instant = Instant.ofEpochSecond(trackingInfo.getShipDateTime());
                ZoneId zone = ZoneId.systemDefault();
                PlatformOrderLogisticsDTO dto = PlatformOrderLogisticsDTO.builder()
                        .code(trackingInfo.getTrackingNumber())
                        .name(trackingInfo.getMethodCode())
                        .deliveryTime(LocalDateTime.ofInstant(instant, zone))
                        .logisticsChannelId(trackingInfo.getCarrierName().getCarrier())
                        .estimatedShippingCost(BigDecimal.ZERO)
                        .actualShippingCost(cost)
                        .accessoriesCostCurrency("")
                        .actualShippingCurrency(currency)
                        .estimatedShippingCurrency("")
                        .build();
                logisticsDTOS.add(dto);
            }
        }
        return logisticsDTOS;
    }

    /**
     * 财务信息表
     * @Author Luo_WG
     * @Date 2023/12/4 14:07
     * @param orderBean
     * @return java.util.List<com.common.business.dto.PlatformOrderFinanceDTO>
     **/
    private static List<PlatformOrderFinanceDTO> parseFinancesList(OrderBean orderBean) {

        List<PlatformOrderFinanceDTO> financeDTOList = new ArrayList<>();
        PlatformOrderFinanceDTO dto = PlatformOrderFinanceDTO.builder()
                .currency("")
                .build();
        financeDTOList.add(dto);
        return financeDTOList;
    }


}
