package com.sdk.oms.mercado.dto;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.utils.StringUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.HttpCommonUtil;
import com.erp.model.oms.enums.MercadoOrderLogisticTypeEnum;
import com.erp.model.oms.enums.OrderLogisticTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.sdk.oms.mercado.dto.mercado.listing.ListingViewDTO;
import com.sdk.oms.mercado.dto.mercado.order.OrderItemsBean;
import com.sdk.oms.mercado.dto.mercado.order.OrderViewDTO;
import com.sdk.oms.mercado.dto.mercado.shipment.ShipmentViewDTO;
import com.sdk.oms.mercado.dto.mercado.shipment.ShippingAddressBeanX;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MercadoOrderDTO extends CleanBaseDTO {

    private OrderViewDTO orderBean;

    private String shopId;

    /**
     * 初始化
     */
    public MercadoOrderDTO(OrderViewDTO orderBean, JobTaskDTO dto, String shopId) {
        this.orderBean = orderBean;
        this.shopId = shopId;
        this.setIsClean(0);
        this.setPlatform(PlatformDictEnum.MERCADOLIBRE.getCode());
        this.setUniqueId(combineUnique(String.valueOf(orderBean.getFid()), this.shopId));
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }

    public static String combineUnique(String orderId, String shopId) {
        return StrUtil.format("{}_{}", orderId, shopId);
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformOrderDTO convertDTO(MercadoOrderDTO dto) {
        // 原商品信息
        return initPlatformProductDTO(dto);
    }

    /**
     * 根据PlatformMercadoListingDTO 转换 DTO
     */
    private static PlatformOrderDTO initPlatformProductDTO(MercadoOrderDTO dto) {
        OrderViewDTO orderBean = dto.getOrderBean();

        //设置对应关系
        PlatformOrderDTO orderDTO = new PlatformOrderDTO();

        //平台订单号
        orderDTO.setPlatformCode(String.valueOf(orderBean.getFid()));

        //销售平台
        orderDTO.setDictPlatform(PlatformDictEnum.MERCADOLIBRE.getCode());

        // 店铺ID
        orderDTO.setShopId(dto.getShopId());


        //付款时间
        //使用 DateTimeFormatter 解析字符串日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        if (CollectionUtils.isNotEmpty(orderBean.getPayments())) {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(orderBean.getPayments().get(0).getDateCreated(), formatter);
            // 转换为 LocalDateTime
            LocalDateTime payTime = offsetDateTime.toLocalDateTime();
            orderDTO.setPayTime(payTime);
            orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode());
            //付款方式
            orderDTO.setDictPayMethod(orderBean.getPayments().get(0).getPaymentMethodId());
        }

        //订单金额
        BigDecimal amount = orderBean.getPayments().stream().map(req -> req.getTotalPaidAmount()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        orderDTO.setAmount(amount);

        //币别
        orderDTO.setCurrency(orderBean.getCurrencyId());

        //买家备注
        orderDTO.setBuyerRemark(orderBean.getFeedback().getPurchase());

        // 是否拦截
        orderDTO.setIsIntercept(false);

        // 拦截备注
        orderDTO.setInterceptRemark("");

        // 来源类型
        orderDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());

        // 来源id
        orderDTO.setSourceId(String.valueOf(orderBean.getFid()));

        // 来源编码
        orderDTO.setSourceCode("");

        //平台创建时间
        if (StringUtils.isNotBlank(orderBean.getDateCreated())) {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(orderBean.getDateCreated(), formatter);
            // 转换为 LocalDateTime
            LocalDateTime createTime = offsetDateTime.toLocalDateTime();
            orderDTO.setPlatformOrderCreateTime(createTime);
        }

        // 标签json
        Map<String, Object> lableMap = new HashMap<>();

        String logisticType = "";
        ShipmentViewDTO shipmentViewDTO = new ShipmentViewDTO();
        if (ObjectUtil.isNotEmpty(orderBean.getShipmentViewDTO())) {
            shipmentViewDTO = orderBean.getShipmentViewDTO();

            if ("me2".equalsIgnoreCase(shipmentViewDTO.getLogistic().getMode()) && MercadoOrderLogisticTypeEnum.FULFILLMENT.getCode().equalsIgnoreCase(shipmentViewDTO.getLogistic().getType())) {
                //如果是平台仓，状态审核通过
                orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE.getCode());
                lableMap.put("logisticType", shipmentViewDTO.getLogistic().getType());
                lableMap.put("isPlatformWarehouseOrder", Boolean.TRUE);
                logisticType = OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode();
            } else if ("me2".equalsIgnoreCase(shipmentViewDTO.getLogistic().getMode())
                    && (MercadoOrderLogisticTypeEnum.DROP_OFF.getCode().equals(shipmentViewDTO.getLogistic().getType()) || MercadoOrderLogisticTypeEnum.CROSS_DOCKING.getCode().equalsIgnoreCase(shipmentViewDTO.getLogistic().getType()))
            ) {
                //中转发货
                lableMap.put("logisticType", shipmentViewDTO.getLogistic().getType());
                logisticType = OrderLogisticTypeEnum.TRANSIT_WAREHOUSE.getCode();
            } else if ("me1".equalsIgnoreCase(shipmentViewDTO.getLogistic().getMode())) {
                //自发货
                lableMap.put("logisticType", MercadoOrderLogisticTypeEnum.DEFAULT.getCode());
                logisticType = OrderLogisticTypeEnum.SELF_SHIPMENT.getCode();
            }

            orderDTO.setLabelJson(JSONUtil.toJsonStr(lableMap));
            //扩展字段
            JSONObject extendDataJson = new JSONObject();
            extendDataJson.put("mode", shipmentViewDTO.getLogistic().getMode());
            extendDataJson.put("logisticType", shipmentViewDTO.getLogistic().getType());
            extendDataJson.put("shipmentId", orderBean.getShipping().getFid());
            orderDTO.setExtendData(extendDataJson.toString());
        }

        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");

        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");
        orderDTO.setInvalidStatus(Boolean.FALSE);
        // 订单状态，详情金额汇总
        if (ObjectUtil.isNotEmpty(orderBean.getShipmentViewDTO())) {
            shipmentViewDTO = orderBean.getShipmentViewDTO();

            if ("handling".equalsIgnoreCase(shipmentViewDTO.getStatus())) {
                orderDTO.setApproveStatusStr(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
                if (logisticType.equals(OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode())) {
                    orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
                    orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE.getStatus());
                } else {
                    orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                }
            } else if ("ready_to_ship".equalsIgnoreCase(shipmentViewDTO.getStatus())) {
                orderDTO.setApproveStatusStr(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
                if (logisticType.equals(OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode())) {
                    orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
                    orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE.getStatus());
                } else {
                    orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                }
            } else if ("shipped".equalsIgnoreCase(shipmentViewDTO.getStatus())) {
                orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE.getStatus());
                orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            } else if ("delivered".equalsIgnoreCase(shipmentViewDTO.getStatus())) {
                orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE.getStatus());
                orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            } else if ("not_delivered".equalsIgnoreCase(shipmentViewDTO.getStatus())) {
                orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE.getStatus());
                orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            } else if ("cancelled".equalsIgnoreCase(shipmentViewDTO.getStatus())) {
                orderDTO.setApproveStatusStr(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
                orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                orderDTO.setInvalidStatus(Boolean.TRUE);
                orderDTO.setRemark("平台取消");
            }
        }

        if ("invalid".equals(orderBean.getStatus())) {
            orderDTO.setApproveStatusStr(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
            orderDTO.setInvalidStatus(Boolean.TRUE);
            orderDTO.setRemark("平台无效订单");
        } else if ("cancelled".equals(orderBean.getStatus())) {
            orderDTO.setApproveStatusStr(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
            orderDTO.setInvalidStatus(Boolean.TRUE);
            orderDTO.setRemark("平台取消");
        } else if ("paid".equals(orderBean.getStatus())) {
            orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode());
        } else {
            orderDTO.setApproveStatusStr(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
        }


        //平台仓审核状态已审核
        if (logisticType.equals(OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode())) {
            orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE.getStatus());
        }


        // 订单明细
        List<PlatformOrderDetailDTO> details = parseDetailDto(orderBean);
        orderDTO.setDetails(details);

        //B2C销售订单买家信息表
        orderDTO.setReceiver(parseReceiver(orderBean));
        //B2C销售订单物流信息表
        orderDTO.setLogisticsList(parseLogistics(orderBean, logisticType));
        //B2C销售订单财务信息表
        orderDTO.setFinances(parseFinances(orderBean));
        // 平台类型
        orderDTO.setPlatform(dto.getPlatform());
        // 唯一ID
        orderDTO.setUniqueId(dto.getUniqueId());
        // 同步任务ID
        orderDTO.setDmpSyncTaskId(dto.getDmpSyncTaskId());
        return orderDTO;
    }

    /**
     * 批量转换明细
     */
    public static List<PlatformOrderDetailDTO> parseDetailDto(OrderViewDTO orderBean) {
        return orderBean.getOrderItems().stream()
                .map(e -> intPlatformOrderDetailDTO(e, orderBean))
                .collect(Collectors.toList());
    }


    /**
     * 转换明细
     */
    private static PlatformOrderDetailDTO intPlatformOrderDetailDTO(OrderItemsBean orderItemsBean, OrderViewDTO orderViewDTO) {
        PlatformOrderDetailDTO detailDTO = new PlatformOrderDetailDTO();
        // 图片URL
        detailDTO.setImageUrl("");
        // skuId
        detailDTO.setSkuId("");
        // skuNo
        detailDTO.setSkuNo("");

        //平台明细行号
        detailDTO.setPlatformLineNumber("");

        // 平台sku编号
        detailDTO.setPlatformSkuNo(orderItemsBean.getItem().getSellerSku());

        //平台产品id
        detailDTO.setPlatformSpuNo(orderItemsBean.getItem().getParentItemId());

        // 库存sku编号
        detailDTO.setWarehouseName("");
        // 仓库名称
        // 库存是否扣除
        detailDTO.setWarehouseId("");
        // 数量
        detailDTO.setQty(orderItemsBean.getQuantity());

        // 金额
        detailDTO.setAmount(orderItemsBean.getFullUnitPrice());
        // 单价
        detailDTO.setPrice(orderItemsBean.getUnitPrice());
        // 币别（原币）
        detailDTO.setCurrency(orderItemsBean.getCurrencyId());
        // 汇率
        detailDTO.setExchangeRate(orderItemsBean.getBaseExchangeRate());
        // 建议售价（本位币）
        detailDTO.setAdvicePrice(BigDecimal.ZERO);
        // 含税成本（本位币）
        detailDTO.setTaxCost(BigDecimal.ZERO);
        // 来源明细id
        detailDTO.setSourceDetailId(orderItemsBean.getItem().getFid());
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
     * 买家信息字段处理
     *
     * @param orderViewDTO
     * @return java.util.List<com.common.business.dto.PlatformOrderReceiverDTO>
     * @Author Luo_WG
     * @Date 2023/12/4 9:38
     **/
    private static PlatformOrderReceiverDTO parseReceiver(OrderViewDTO orderViewDTO) {
        if (Objects.isNull(orderViewDTO) || Objects.isNull(orderViewDTO.getShipping())) {
            return null;
        }
        ShippingAddressBeanX shippingAddress = orderViewDTO.getShipmentViewDTO().getDestination().getShippingAddress();
        return PlatformOrderReceiverDTO.builder()
                .loginId(String.valueOf(orderViewDTO.getBuyer().getFid()))
                .customerId(String.valueOf(orderViewDTO.getBuyer().getFid()))
                .name(orderViewDTO.getBuyer().getFirstName() + " " + orderViewDTO.getBuyer().getLastName())
                .receiverName(orderViewDTO.getShipmentViewDTO().getDestination().getReceiverName())
                .telNumber(orderViewDTO.getShipmentViewDTO().getDestination().getReceiverPhone())
                .receiverTelNumber(orderViewDTO.getShipmentViewDTO().getDestination().getReceiverPhone())
                .email("")
                .country(shippingAddress.getCountry().getFid())
                .provinceName(shippingAddress.getState().getName())
                .cityName(shippingAddress.getCity().getName())
                .districtName(shippingAddress.getAddressLine())
                .postCode(shippingAddress.getZipCode())
                .firstAddress(shippingAddress.getNeighborhood().getName() + " " +
                        (StringUtils.isNotBlank(shippingAddress.getMunicipality().getName()) ? shippingAddress.getMunicipality().getName() : "") + " " +
                        shippingAddress.getComment())
                .secondAddress("")
                .fullAddress(shippingAddress.getNeighborhood().getName() + " " +
                        (StringUtils.isNotBlank(shippingAddress.getMunicipality().getName()) ? shippingAddress.getMunicipality().getName() : "") + " " +
                        shippingAddress.getComment())
                .build();
    }


    /**
     * 物流信息字段处理
     *
     * @param orderBean
     * @return java.util.List<com.common.business.dto.PlatformOrderLogisticsDTO>
     * @Author Luo_WG
     * @Date 2023/12/4 14:07
     **/
    private static List<PlatformOrderLogisticsDTO> parseLogistics(OrderViewDTO orderBean, String logisticType) {
        if (ObjectUtil.isEmpty(orderBean)) {
            return Collections.emptyList();
        }

        String trackingNumber = "";
        String name = "";
        BigDecimal cost = BigDecimal.ZERO;
        LocalDateTime deliveryTime = null;
        if (ObjectUtil.isNotEmpty(orderBean.getShipmentViewDTO())) {
            trackingNumber = orderBean.getShipmentViewDTO().getTrackingNumber();
            name = orderBean.getShipmentViewDTO().getLeadTime().getShippingMethod().getName();
            cost = orderBean.getShipmentViewDTO().getLeadTime().getCost();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(orderBean.getShipmentViewDTO().getDateCreated(), formatter);
            //发货时间
            deliveryTime = offsetDateTime.toLocalDateTime();
        }


        List<PlatformOrderLogisticsDTO> logisticsDTOS = new ArrayList<>();
        //自发货不用更新物流单
        if (!logisticType.equals(OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode())) {
            trackingNumber = "";
        }
        BigDecimal shippingCost = orderBean.getPayments().stream().map(req -> req.getShippingCost()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

        PlatformOrderLogisticsDTO dto = PlatformOrderLogisticsDTO.builder()
                .code(trackingNumber)
                .name(name)
                .deliveryTime(deliveryTime)
                .logisticsChannelId("")
                .logisticsChannelName("")
                .estimatedShippingCost(cost)
                .actualShippingCost(shippingCost)
                .accessoriesCostCurrency("")
                .actualShippingCurrency("")
                .estimatedShippingCurrency("")
                .logisticType(logisticType)
                .build();
        logisticsDTOS.add(dto);
        return logisticsDTOS;
    }

    /**
     * 财务信息表
     *
     * @param orderViewDTO
     * @return java.util.List<com.common.business.dto.PlatformOrderFinanceDTO>
     * @Author Luo_WG
     * @Date 2023/12/4 14:07
     **/
    private static PlatformOrderFinanceDTO parseFinances(OrderViewDTO orderViewDTO) {
        BigDecimal vatRate = orderViewDTO.getPayments().stream().map(req -> req.getTaxesAmount()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        BigDecimal shippingCost = orderViewDTO.getPayments().stream().map(req -> req.getShippingCost()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        return PlatformOrderFinanceDTO.builder()
                .currency("")
                .shippingCost(shippingCost)
                .vatRate(vatRate)
                .build();
    }

}
