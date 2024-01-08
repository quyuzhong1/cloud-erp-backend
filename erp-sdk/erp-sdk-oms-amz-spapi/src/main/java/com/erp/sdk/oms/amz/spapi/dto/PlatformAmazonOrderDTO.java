package com.erp.sdk.oms.amz.spapi.dto;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.*;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.sdk.oms.amz.spapi.model.orders.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 平台亚马逊订单DTO
 *
 * @Author Cloud
 * @Date 2023/8/31 16:01
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformAmazonOrderDTO extends CleanBaseDTO {

    private Order order;

    @Panno(findType = PannoEnum.EQ,field = "shopId")
    private String shopId;

    /**
     * 数据下载状态
     * 0 详情数据需要更新
     * 1 详情数据已更新
     */
    @Panno(findType = PannoEnum.EQ,field = "downloadStatus")
    private Integer downloadStatus;

    /**
     * 地址详情下载状态
     * 0 详情数据需要更新
     * 1 详情数据已更新
     */
    @Panno(findType = PannoEnum.EQ,field = "downloadAddressStatus")
    private Integer downloadAddressStatus;

    /**
     * 订单明细
     */
    private OrderItemList details;

    /**
     * redisson执行中key
     */
    private String redissonKey;

    public PlatformAmazonOrderDTO(Order order, String shopId) {
        this.order = order;
        this.shopId = shopId;
        this.setUniqueId(order.getAmazonOrderId());
        this.setPlatform(PlatformDictEnum.AMAZON.getCode());
        this.setDownloadStatus(0);
        this.setDownloadAddressStatus(0);
        this.setIsClean(CleanStatusEnum.NONE.getCode());
    }

    public static PlatformAmazonOrderDTO getByAddressDownloadStatus() {
        PlatformAmazonOrderDTO orderMongoDTO = new PlatformAmazonOrderDTO();
        orderMongoDTO.setDownloadStatus(1);
        orderMongoDTO.setDownloadAddressStatus(0);
        return orderMongoDTO;
    }

    /**
     * 转换目标实体:PlatformProductDTO
     */
    public static PlatformOrderDTO convertDTO(PlatformAmazonOrderDTO dto, Boolean isSendMq) {
        // 原订单信息
        Order sourceOrder = dto.getOrder();

        PlatformOrderDTO orderDTO = new PlatformOrderDTO();
        BeanUtils.copyProperties(dto, orderDTO);

        // 来源类型
        orderDTO.setSourceType("soB2c");
        // 来源id
        orderDTO.setSourceId(sourceOrder.getAmazonOrderId());
        // 来源编码
        orderDTO.setSourceCode("");
        // 标签json
        Map<String, String> lableMap = new HashMap<>();
        if (Order.FulfillmentChannelEnum.AFN.getValue().equalsIgnoreCase(dto.getOrder().getFulfillmentChannel().getValue())) {
            lableMap.put("fulfillmentChannel", "AFN");
        }
        if (Order.OrderStatusEnum.UNFULFILLABLE.getValue().equalsIgnoreCase(dto.getOrder().getOrderStatus())){
            lableMap.put("amazonStatus", "Unfulfillable");
        }
        orderDTO.setLabelJson(JSONUtil.toJsonStr(lableMap));


        // 订单日期
        LocalDateTime purchaseLocalDateTime = sourceOrder.convertPurchaseLocalDateTime();
        orderDTO.setBillDate(purchaseLocalDateTime.toLocalDate());
        // 平台订单号
        orderDTO.setPlatformCode(sourceOrder.getAmazonOrderId());
        // 销售平台
        orderDTO.setDictPlatform(PlatformDictEnum.AMAZON.getCode());
        // 店铺ID
        orderDTO.setShopId(dto.getShopId());

        // 作废状态（false未作废，true已作废）
        orderDTO.setInvalidStatus(sourceOrder.convertCancel());
        // 作废类型（manual手动作废，automatic自动作废）
        orderDTO.setInvalidType(orderDTO.getInvalidStatus() ? "automatic" : "");
        // 作废原因
        orderDTO.setInvalidRemark("");
        // 订单状态
        // （soB2cBillStatus字典类型）
        orderDTO.setBillStatus(sourceOrder.convertBillStatus());
        // 付款状态（待付款、已付款）
        // （soB2cPayStatus字典类型）
        orderDTO.setPayStatus(sourceOrder.convertPayStatus());
        // 审核状态
        orderDTO.setApproveStatusStr(sourceOrder.convertApproveStatusStr());

        // 订单金额
        orderDTO.setAmount(null == sourceOrder.getOrderTotal() ? BigDecimal.ZERO : new BigDecimal(sourceOrder.getOrderTotal().getAmount()));
        // 币别（原币）
        orderDTO.setCurrency(null == sourceOrder.getOrderTotal() ? "" : sourceOrder.getOrderTotal().getCurrencyCode());
        // 汇率
        orderDTO.setExchangeRate(BigDecimal.ZERO);
        //  运费
        BigDecimal shippingFee = BigDecimal.ZERO;
        orderDTO.setShippingFee(shippingFee);
        // 付款时间
        // 未付款无付款时间
        orderDTO.setPayTime("payment".equalsIgnoreCase(orderDTO.getPayStatus()) ? null : purchaseLocalDateTime);
        // 付款金额
        orderDTO.setPayAmount(null == sourceOrder.getOrderTotal() ? BigDecimal.ZERO : new BigDecimal(sourceOrder.getOrderTotal().getAmount()));
        // 付款方式
        orderDTO.setDictPayMethod(null == sourceOrder.getPaymentMethod() ? "":sourceOrder.getPaymentMethod().getValue());
        // 买家备注
        orderDTO.setBuyerRemark("");
        // 订单备注
        orderDTO.setRemark("");
        // 销售组织id
        orderDTO.setOrgId("");
        // 销售组织名称
        orderDTO.setOrgName("");
        // 是否拦截
        orderDTO.setIsIntercept(false);
        // 拦截备注
        orderDTO.setInterceptRemark("");

        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");
        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");
        // 数据下载状态:
        // 0=详情数据需要更新(不发送MQ)
        // 1=详情数据已更新(发送MQ)
        orderDTO.setDownloadStatus(0);
        // 记录详情
        if (!CollectionUtils.isEmpty(dto.getDetails())){
            List<PlatformOrderDetailDTO> detailDTO = dto.getDetails().stream()
                    .map(PlatformAmazonOrderDTO::intPlatformOrderDetailDTO)
                    .collect(Collectors.toList());
            orderDTO.setDetails(detailDTO);
            orderDTO.setDownloadStatus(1);
        }


        // 订单财务信息
        if (!CollectionUtils.isEmpty(dto.getDetails())) {
            final String[] currency = {""};
//            final BigDecimal[] shippingCost = {BigDecimal.ZERO};
            dto.getDetails().forEach(e-> {
                Money money = e.getShippingPrice();
                if (null == money){
                    return;
                }
                currency[0] = money.getCurrencyCode();
//                shippingCost[0] = shippingCost[0].add(new BigDecimal(money.getAmount()));
            });
            PlatformOrderFinanceDTO financeDTO = new PlatformOrderFinanceDTO();
            // 亚马逊物流费用不显示
            financeDTO.setShippingCost(BigDecimal.ZERO);
            financeDTO.setCurrency(currency[0]);
            orderDTO.setFinances(financeDTO);
        }

        // 订单物流信息(无)

        // 订单买家信息
        BuyerInfo buyerInfo = dto.getOrder().getBuyerInfo();
        if (null != dto.getOrder().getBuyerInfo()){
            PlatformOrderReceiverDTO receiverDTO = new PlatformOrderReceiverDTO();
            receiverDTO.setEmail(StringUtils.isBlank(buyerInfo.getBuyerEmail()) ? "" : buyerInfo.getBuyerEmail());
            Address shippingAddress = dto.getOrder().getShippingAddress();
            if (null != shippingAddress){
                receiverDTO.setName(StringUtils.isBlank(shippingAddress.getName()) ? "" : shippingAddress.getName());
                receiverDTO.setFirstAddress(StringUtils.isBlank(shippingAddress.getAddressLine1()) ? "" : shippingAddress.getAddressLine1());
                String secondAddress = StrUtil.concat(true, shippingAddress.getAddressLine2(), shippingAddress.getAddressLine3());
                receiverDTO.setSecondAddress(secondAddress);

                receiverDTO.setReceiverTelNumber(StringUtils.isBlank(shippingAddress.getPhone()) ? "" : shippingAddress.getPhone());
                receiverDTO.setTelNumber(StringUtils.isBlank(shippingAddress.getPhone()) ? "" : shippingAddress.getPhone());
                receiverDTO.setCityName(StringUtils.isBlank(shippingAddress.getCity()) ? "" : shippingAddress.getCity());
                receiverDTO.setCountry(StringUtils.isBlank(shippingAddress.getCountryCode()) ? "" : shippingAddress.getCountryCode());
                receiverDTO.setReceiverName(StringUtils.isBlank(shippingAddress.getName()) ? "" : shippingAddress.getName());
                // 区域
                receiverDTO.setDistrictName(StringUtils.isBlank(shippingAddress.getDistrict()) ? "" : shippingAddress.getDistrict());
                // 省/州
                receiverDTO.setProvinceName(StringUtils.isBlank(shippingAddress.getStateOrRegion()) ? "" : shippingAddress.getStateOrRegion());

                String fullAddress = StrUtil.concat(true,  shippingAddress.getMunicipality());
                receiverDTO.setFullAddress(fullAddress);
                receiverDTO.setPostCode(shippingAddress.getPostalCode());

            }
            orderDTO.setReceiver(receiverDTO);
        }
        return orderDTO;
    }

    /**
     * 转换明细
     */
    public static PlatformOrderDetailDTO intPlatformOrderDetailDTO(OrderItem item) {
        PlatformOrderDetailDTO detailDTO = new PlatformOrderDetailDTO();
        // 图片URL
        detailDTO.setImageUrl("");
        // skuId
        detailDTO.setSkuId("");
        // skuNo
        detailDTO.setSkuNo("");

        // 平台sku编号
        detailDTO.setPlatformSkuNo(item.getSellerSKU());

        // 平台产品id
        detailDTO.setPlatformSpuNo(item.getASIN());
        // 库存sku编号
        detailDTO.setWarehouseSkuNo("");
        detailDTO.setWarehouseName("");
        // 仓库名称
        // 库存是否扣除
        detailDTO.setWarehouseId("");
        // 数量
        detailDTO.setQty(item.getQuantityOrdered());
        // item总价
        Money itemPrice = item.getItemPrice();
        // 金额
        detailDTO.setAmount(new BigDecimal(null == itemPrice ? "0" : itemPrice.getAmount()));

        // 计算单价
        BigDecimal price = BigDecimal.ZERO;
        if (null != item.getQuantityOrdered() && 0 < item.getQuantityOrdered()){
            price = detailDTO.getAmount().divide(BigDecimal.valueOf(item.getQuantityOrdered()), 2, RoundingMode.DOWN);
        }

        // 单价
        detailDTO.setPrice(price);

        // 币别（原币）
        detailDTO.setCurrency(null == itemPrice ? "" : itemPrice.getCurrencyCode());
        // 汇率
        detailDTO.setExchangeRate(BigDecimal.ONE);
        // 建议售价（本位币）
        detailDTO.setAdvicePrice(BigDecimal.ZERO);
        // 含税成本（本位币）
        detailDTO.setTaxCost(BigDecimal.ZERO);
        // 来源明细id
        detailDTO.setSourceDetailId(item.getOrderItemId());
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
