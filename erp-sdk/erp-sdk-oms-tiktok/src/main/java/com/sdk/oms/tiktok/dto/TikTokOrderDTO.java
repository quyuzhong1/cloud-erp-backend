package com.sdk.oms.tiktok.dto;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.sdk.oms.tiktok.dto.tiktok.order.view.DistrictInfoBean;
import com.sdk.oms.tiktok.dto.tiktok.order.view.LineItemsBean;
import com.sdk.oms.tiktok.dto.tiktok.order.view.OrderViewDTO;
import com.sdk.oms.tiktok.dto.tiktok.order.view.OrdersBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TikTokOrderDTO extends CleanBaseDTO {

    private OrdersBean orderViewDTO;

    private String shopId;

    /**
     * 初始化
     */
    public TikTokOrderDTO(OrdersBean orderViewDTO, JobTaskDTO dto, String shopId) {
        this.orderViewDTO = orderViewDTO;
        this.shopId = shopId;
        this.setIsClean(0);
        this.setPlatform(PlatformDictEnum.MERCADOLIBRE.getCode());
        this.setUniqueId(combineUnique(orderViewDTO.getFid(), this.shopId));
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }

    public static String combineUnique(String orderId, String shopId){
        return StrUtil.format("{}_{}", orderId, shopId);
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformOrderDTO convertDTO(TikTokOrderDTO dto) {
        // 原商品信息
        return initPlatformProductDTO(dto);
    }

    /**
     * 根据PlatformMercadoListingDTO 转换 DTO
     */
    private static PlatformOrderDTO initPlatformProductDTO(TikTokOrderDTO dto) {
        OrdersBean ordersBean = dto.getOrderViewDTO();

        //设置对应关系
        PlatformOrderDTO orderDTO = new PlatformOrderDTO();

        //平台订单号
        orderDTO.setPlatformCode(String.valueOf(ordersBean.getFid()));

        //销售平台
        orderDTO.setDictPlatform(PlatformDictEnum.TIK_TOK.getCode());

        // 店铺ID
        orderDTO.setShopId(dto.getShopId());

        //订单金额
        BigDecimal amount = NumberUtil.toBigDecimal(ordersBean.getPayment().getTotalAmount());
        orderDTO.setAmount(amount);
        //币别
        orderDTO.setCurrency(ordersBean.getPayment().getCurrency());

        //付款时间
        //使用 DateTimeFormatter 解析字符串日期
        if (ObjectUtil.isNotEmpty(ordersBean.getPaidTime())) {
            // 使用Instant类将Unix时间戳转换为LocalDateTime对象
            LocalDateTime payTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(ordersBean.getPaidTime()), ZoneOffset.UTC);
            orderDTO.setPayTime(payTime);

            //付款方式
            orderDTO.setDictPayMethod(ordersBean.getPaymentMethodName());
        }

        //买家备注
        orderDTO.setBuyerRemark(ordersBean.getBuyerMessage());

        // 是否拦截
        orderDTO.setIsIntercept(false);

        // 拦截备注
        orderDTO.setInterceptRemark("");

        // 来源类型
        orderDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());

        // 来源id
        orderDTO.setSourceId(String.valueOf(ordersBean.getFid()));

        // 来源编码
        orderDTO.setSourceCode("");

        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");

        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");
        orderDTO.setInvalidStatus(Boolean.FALSE);

        if ("ON_HOLD".equalsIgnoreCase(ordersBean.getStatus())) {
            orderDTO.setApproveStatusStr(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_FROZEN.getCode());
            orderDTO.setRemark("ON_HOLD");
        } else if ("AWAITING_SHIPMENT".equalsIgnoreCase(ordersBean.getStatus())) {
            orderDTO.setApproveStatusStr(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode());
            orderDTO.setRemark("");
        } else if ("AWAITING_COLLECTION".equalsIgnoreCase(ordersBean.getStatus())) {
            orderDTO.setApproveStatusStr(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode());
            orderDTO.setRemark("");
        } else if ("PARTIALLY_SHIPPING".equalsIgnoreCase(ordersBean.getStatus())) {
            orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE.getStatus());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
            orderDTO.setRemark("");
        } else if ("IN_TRANSIT".equalsIgnoreCase(ordersBean.getStatus())) {
            orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE.getStatus());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            orderDTO.setRemark("");
        } else if ("DELIVERED".equalsIgnoreCase(ordersBean.getStatus())) {
            orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE.getStatus());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            orderDTO.setRemark("");
        } else if ("COMPLETED".equalsIgnoreCase(ordersBean.getStatus())) {
            orderDTO.setApproveStatusStr(ApproveStatusEnum.APPROVE.getStatus());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            orderDTO.setRemark("");
        } else if ("CANCELLED".equalsIgnoreCase(ordersBean.getStatus())) {
            orderDTO.setApproveStatusStr(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
            orderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
            orderDTO.setInvalidStatus(Boolean.TRUE);
            orderDTO.setRemark("平台取消");
        }


        // 订单明细
        List<PlatformOrderDetailDTO> details = parseDetailDto(ordersBean);
        orderDTO.setDetails(details);

        //B2C销售订单买家信息表
        orderDTO.setReceiver(parseReceiver(ordersBean));
        //B2C销售订单物流信息表
        orderDTO.setLogisticsList(parseLogistics(ordersBean));
        //B2C销售订单财务信息表
        orderDTO.setFinances(parseFinances(ordersBean));
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
    public static List<PlatformOrderDetailDTO> parseDetailDto(OrdersBean ordersBean) {
        return ordersBean.getLineItems().stream()
                .map(e -> intPlatformOrderDetailDTO(e, ordersBean))
                .collect(Collectors.toList());
    }


    /**
     * 转换明细
     */
    private static PlatformOrderDetailDTO intPlatformOrderDetailDTO(LineItemsBean itemsBean, OrdersBean ordersBean) {
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
        detailDTO.setPlatformSkuNo(itemsBean.getSellerSku());

        //平台产品id
        detailDTO.setPlatformSpuNo(itemsBean.getSkuId());

        // 库存sku编号
        detailDTO.setWarehouseName("");
        // 仓库名称
        // 库存是否扣除
        detailDTO.setWarehouseId("");
        // 数量
        detailDTO.setQty(1);

        // 金额
        detailDTO.setAmount(NumberUtil.toBigDecimal(itemsBean.getSalePrice()));
        // 单价
        detailDTO.setPrice(NumberUtil.toBigDecimal(itemsBean.getOriginalPrice()));
        // 币别（原币）
        detailDTO.setCurrency(itemsBean.getCurrency());
        // 汇率
        detailDTO.setExchangeRate(BigDecimal.ZERO);
        // 建议售价（本位币）
        detailDTO.setAdvicePrice(BigDecimal.ZERO);
        // 含税成本（本位币）
        detailDTO.setTaxCost(BigDecimal.ZERO);
        // 来源明细id
        detailDTO.setSourceDetailId(itemsBean.getFid());
        // 标签json
        detailDTO.setLabelJson("");
        // 库存组织id
        detailDTO.setWarehouseOrgId("");
        // 库存组织名称
        detailDTO.setWarehouseOrgName("");
        // 库位
        detailDTO.setWarehouseLocation("");
        //包裹号
        detailDTO.setPlatformPackageId(itemsBean.getPackageId());
        return detailDTO;
    }


    /**
     * 买家信息字段处理
     * @Author Luo_WG
     * @Date 2023/12/4 9:38
     * @param ordersBean
     * @return java.util.List<com.common.business.dto.PlatformOrderReceiverDTO>
     **/
    private static PlatformOrderReceiverDTO parseReceiver(OrdersBean ordersBean) {
        if (Objects.isNull(ordersBean)) {
            return null;
        }
        String state = "";
        String city = "";
        String district = "";
        String Community = "";
        for (DistrictInfoBean districtInfoBean : ordersBean.getRecipientAddress().getDistrictInfo()) {
            if ("state".equalsIgnoreCase(districtInfoBean.getAddressLevelName())) {
                state = districtInfoBean.getAddressName();
            }
            if ("city".equalsIgnoreCase(districtInfoBean.getAddressLevelName())) {
                city = districtInfoBean.getAddressName();
            }
            if ("Sub-district".equalsIgnoreCase(districtInfoBean.getAddressLevelName())) {
                district = districtInfoBean.getAddressName();
            }
            if ("Urban Community".equalsIgnoreCase(districtInfoBean.getAddressLevelName())) {
                Community = districtInfoBean.getAddressName();
            }

        }
        return PlatformOrderReceiverDTO.builder()
                .loginId(String.valueOf(ordersBean.getUserId()))
                .customerId(ordersBean.getUserId())
                .name(ordersBean.getRecipientAddress().getName())
                .receiverName(ordersBean.getRecipientAddress().getName())
                .telNumber(ordersBean.getRecipientAddress().getPhoneNumber())
                .receiverTelNumber(ordersBean.getRecipientAddress().getPhoneNumber())
                .email(ordersBean.getBuyerEmail())
                .country(ordersBean.getRecipientAddress().getRegionCode())
                .provinceName(state)
                .cityName(city)
                .districtName(district+" "+Community)
                .postCode(ordersBean.getRecipientAddress().getPostalCode())
                .firstAddress(ordersBean.getRecipientAddress().getAddressLine1())
                .secondAddress(ordersBean.getRecipientAddress().getAddressLine2()+ordersBean.getRecipientAddress().getAddressLine3()+ordersBean.getRecipientAddress().getAddressLine4())
                .fullAddress(ordersBean.getRecipientAddress().getAddressDetail())
                .receiverTaxNo(ordersBean.getCpf())
                .build();
    }


    /**
     * 物流信息字段处理
     * @Author Luo_WG
     * @Date 2023/12/4 14:07
     * @param ordersBean
     * @return java.util.List<com.common.business.dto.PlatformOrderLogisticsDTO>
     **/
    private static List<PlatformOrderLogisticsDTO> parseLogistics(OrdersBean ordersBean) {
        if (ObjectUtil.isEmpty(ordersBean)) {
            return Collections.emptyList();
        }

        String name = "";
        BigDecimal cost = BigDecimal.ZERO;


        List<PlatformOrderLogisticsDTO> logisticsDTOS = new ArrayList<>();
        PlatformOrderLogisticsDTO dto = PlatformOrderLogisticsDTO.builder()
                .code(ordersBean.getTrackingNumber())
                .name(name)
                .deliveryTime(ordersBean.getRtsTime()>0 ? LocalDateTime.ofInstant(Instant.ofEpochSecond(ordersBean.getRtsTime()), ZoneOffset.UTC) : null)
                .logisticsChannelId(ordersBean.getShippingProviderId())
                .logisticsChannelName(ordersBean.getShippingProvider())
                .estimatedShippingCost(cost)
                .actualShippingCost(BigDecimal.ZERO)
                .accessoriesCostCurrency("")
                .actualShippingCurrency("")
                .estimatedShippingCurrency("")
                .logisticType(ordersBean.getShippingType())
                .build();
        logisticsDTOS.add(dto);
        return logisticsDTOS;
    }

    /**
     * 财务信息表
     * @Author Luo_WG
     * @Date 2023/12/4 14:07
     * @param ordersBean
     * @return java.util.List<com.common.business.dto.PlatformOrderFinanceDTO>
     **/
    private static PlatformOrderFinanceDTO parseFinances(OrdersBean ordersBean) {
        return PlatformOrderFinanceDTO.builder()
                .currency(ordersBean.getPayment().getCurrency())
                .shippingCost(NumberUtil.toBigDecimal(ordersBean.getPayment().getShippingFee()))
                .build();
    }
}
